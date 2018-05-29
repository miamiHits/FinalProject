package FinalProject.BL.Agents;

import FinalProject.BL.DataObjects.Actuator;
import FinalProject.BL.DataObjects.Prefix;
import FinalProject.BL.DataObjects.RelationType;
import FinalProject.BL.DataObjects.Sensor;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PropertyWithData {
    private String name;
    private double min;
    private double max;
    private double targetValue;
    private Actuator actuator;
    private Sensor sensor;
    private boolean isPassiveOnly = false;
    private Prefix prefix;
    private RelationType rt;
    private double targetTick;
    private double deltaWhenWork;
    private double powerConsumedInWork;
    private double deltaWhenWorkOffline;
    private boolean isLoaction = true;
    public  Map<String,Double> relatedSensorsDelta = new HashMap<>();
    public  Map<String,Double> relatedSensorsWhenWorkOfflineDelta = new HashMap<>();
    public List<Integer> activeTicks = new ArrayList<>();
    private final static Logger logger = Logger.getLogger(PropertyWithData.class);
    private double cachedSensorState;

    public double getCachedSensorState() {
        return cachedSensorState;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public double getTargetValue() {
        return targetValue;
    }

    public void setTargetValue(double targetValue) {
        this.targetValue = targetValue;
    }

    public Actuator getActuator() {
        return actuator;
    }

    public void setActuator(Actuator actuator) {
        this.actuator = actuator;
    }

    public Sensor getSensor() {
        return sensor;
    }

    public void setSensor(Sensor sensor) {
        this.sensor = sensor;
    }

    public boolean isPassiveOnly() {
        return isPassiveOnly;
    }

    public void setPassiveOnly(boolean passiveOnly) {
        isPassiveOnly = passiveOnly;
    }

    public Prefix getPrefix() {
        return prefix;
    }

    public void setPrefix(Prefix prefix) {
        this.prefix = prefix;
    }

    public RelationType getRt() {
        return rt;
    }

    public void setRt(RelationType rt) {
        this.rt = rt;
    }

    public double getTargetTick() {
        return targetTick;
    }

    public void setTargetTick(double targetTick) {
        this.targetTick = targetTick;
    }

    public double getDeltaWhenWork() {
        return deltaWhenWork;
    }

    public void setDeltaWhenWork(double deltaWhenWork) {
        this.deltaWhenWork = deltaWhenWork;
    }

    public double getDeltaWhenWorkOffline() {
        return deltaWhenWorkOffline;
    }

    public void setDeltaWhenWorkOffline(double deltaWhenWorkOffline) {
        this.deltaWhenWorkOffline = deltaWhenWorkOffline;
    }

    public boolean isLoaction() {
        return isLoaction;
    }

    public void setLoaction(boolean loaction) {
        isLoaction = loaction;
    }

    public Map<String, Double> getRelatedSensorsDelta() {
        return relatedSensorsDelta;
    }

    public Map<String, Double> getRelatedSensorsWhenWorkOfflineDelta() {
        return relatedSensorsWhenWorkOfflineDelta;
    }

    public double getPowerConsumedInWork() {
        return powerConsumedInWork;
    }

    public void setPowerConsumedInWork(double powerConsumedInWork) {
        this.powerConsumedInWork = powerConsumedInWork;
    }

    public void setCachedSensor(double cachedSensor) {
        this.cachedSensorState = cachedSensor; }

    public PropertyWithData () {}

    /**
     *  scans the designated interval and add activations due to offline(the actuator.state = off) delta that is not 0
     * @param targetTickToCount - the upper bound of the interval that is under examination
     * @param powerConsumption - current power consumption that might be modified due to additional activations resulted from offline delta != 0
     * @param isFromStart - consider the the target tick mentioned in the rule to be a pivot for the horizon
     *                    this argument indicates if the bottom partition will be checked:
     *                    true results scan 0 -> targetTick | false results targetTick -> targetToCount
     */
    public void calcAndUpdateCurrState(double targetTickToCount, double[] powerConsumption, boolean isFromStart) {

        double currState = sensor.getCurrentState(), newState;
        if (isFromStart) {
            // here AFTER so we take care from 0 to target tick. We look at curr state val
            newState = currState + ((targetTick - targetTickToCount) * deltaWhenWorkOffline);
        }
        else {
            // here BEFORE so we take care from target tick TO FINAL tick. We look at target val as state
            newState = currState + ((targetTickToCount - targetTick) * deltaWhenWorkOffline);
        }

        if (newState == currState) {
            return ; //no offline work on these ticks
        }
        else{
            List<Integer> tempActiveTicks = new ArrayList<>();
            int low, high;
            if (isFromStart) {
                low=(int) targetTickToCount;
                high = (int) targetTick-1;
            }
            else{
                low = (int) targetTick;
                high = (int) targetTickToCount;
            }
            List<Integer> rangeTicks = IntStream.rangeClosed(low, high)
                    .boxed().collect(Collectors.toList());

            for ( ; low<= high; ++low) {
                if (currState < this.min) {
                    //lets go back tick before the change.
                    low--;
                    currState -= deltaWhenWorkOffline;
                    //now lets charge it to the maximum point
                    double ticksToCharge = Math.ceil((max - currState) / deltaWhenWork);
                    if (ticksToCharge + low > (powerConsumption.length-1)) {
                         ticksToCharge = (powerConsumption.length-1) - low ;
                    }
                    currState = updateValueToSensor(powerConsumption, currState, ticksToCharge, low, isFromStart, tempActiveTicks);

                    low = (int)ticksToCharge + low + 1 ;
                    if (low>= high) break;

                }

                currState += deltaWhenWorkOffline;
            }

            int size = tempActiveTicks.size();
            if (size > 0)
             {

                 for (int i=0; i<size; i++)
                 {
                     powerConsumption[tempActiveTicks.get(i)]-= powerConsumedInWork;
                 }
                 Collections.shuffle(rangeTicks);

                 List<Integer> sublist = rangeTicks.subList(0, size);
                 for (int i=0; i<size; i++)
                 {
                     powerConsumption[sublist.get(i)]+= powerConsumedInWork;
                 }

                 this.activeTicks.addAll(sublist);
                 //logger.warn("YARDEN DEBUG: AFTER active ticks are: " + this.activeTicks.toString());

             }

            //update the curr state now
            Map<Sensor, Double> toSend = new HashMap<>();
            toSend.put(sensor, currState);
            actuator.act(toSend);
        }
    }


    /**
     *
     * @param iterationPowerConsumption - current consumption prior the update
     * @param newState - current sensor's state what will be updated in the method
     * @param ticksToCharge - how many additional ticks of activation are required
     * @param idxTicks - the base index on the horizon from which the additional activations will be added
     * @param offlineWork - is the work offline (work done to compensate for negative deltas)
     * @param tempActiveTicks - temp list of the ticks, after we'll rand it they will be added to "ActiveTicks" list of the prop
     * @return the new state of the sensor after the latest activation(the same as newState if no additional activation was required)
     */
    public double updateValueToSensor(double[] iterationPowerConsumption, double newState, double ticksToCharge, int idxTicks, boolean offlineWork, List<Integer> tempActiveTicks)
    {
        for (int j = 1; j <= ticksToCharge; ++j) {
            //update the powerCons array
            iterationPowerConsumption[j + idxTicks] += powerConsumedInWork;
            newState += deltaWhenWork;
            if(!offlineWork) {
                tempActiveTicks.add(j + idxTicks);
            }
        }
        if (newState > max) {
            newState = max;
        }

        //update the sensor through the actuator
        Map<Sensor, Double> sensorToStateMap = new HashMap<>();
        sensorToStateMap.put(sensor, newState);
        actuator.act(sensorToStateMap);

        return newState;
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        PropertyWithData that = (PropertyWithData) o;

        if (Double.compare(that.getMin(), getMin()) != 0)
        {
            return false;
        }
        if (Double.compare(that.getMax(), getMax()) != 0)
        {
            return false;
        }
        if (Double.compare(that.getTargetValue(), getTargetValue()) != 0)
        {
            return false;
        }
        if (isPassiveOnly() != that.isPassiveOnly())
        {
            return false;
        }
        if (Double.compare(that.getTargetTick(), getTargetTick()) != 0)
        {
            return false;
        }
        if (Double.compare(that.getDeltaWhenWork(), getDeltaWhenWork()) != 0)
        {
            return false;
        }
        if (Double.compare(that.getPowerConsumedInWork(), getPowerConsumedInWork()) != 0)
        {
            return false;
        }
        if (Double.compare(that.getDeltaWhenWorkOffline(), getDeltaWhenWorkOffline()) != 0)
        {
            return false;
        }
        if (isLoaction() != that.isLoaction())
        {
            return false;
        }
        if (Double.compare(that.getCachedSensorState(), getCachedSensorState()) != 0)
        {
            return false;
        }
        if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null)
        {
            return false;
        }
        if (getActuator() != null ? !getActuator().equals(that.getActuator()) : that.getActuator() != null)
        {
            return false;
        }
        if (getSensor() != null ? !getSensor().equals(that.getSensor()) : that.getSensor() != null)
        {
            return false;
        }
        if (getPrefix() != that.getPrefix())
        {
            return false;
        }
        if (getRt() != that.getRt())
        {
            return false;
        }
        if (getRelatedSensorsDelta() != null ? !getRelatedSensorsDelta().equals(that.getRelatedSensorsDelta()) : that.getRelatedSensorsDelta() != null)
        {
            return false;
        }
        if (getRelatedSensorsWhenWorkOfflineDelta() != null ? !getRelatedSensorsWhenWorkOfflineDelta().equals(that.getRelatedSensorsWhenWorkOfflineDelta()) : that.getRelatedSensorsWhenWorkOfflineDelta() != null)
        {
            return false;
        }
        return activeTicks != null ? activeTicks.equals(that.activeTicks) : that.activeTicks == null;

    }

    @Override
    public int hashCode()
    {
        int result;
        long temp;
        result = getName() != null ? getName().hashCode() : 0;
        temp = Double.doubleToLongBits(getMin());
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(getMax());
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(getTargetValue());
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (getActuator() != null ? getActuator().hashCode() : 0);
        result = 31 * result + (getSensor() != null ? getSensor().hashCode() : 0);
        result = 31 * result + (isPassiveOnly() ? 1 : 0);
        result = 31 * result + (getPrefix() != null ? getPrefix().hashCode() : 0);
        result = 31 * result + (getRt() != null ? getRt().hashCode() : 0);
        temp = Double.doubleToLongBits(getTargetTick());
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(getDeltaWhenWork());
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(getPowerConsumedInWork());
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(getDeltaWhenWorkOffline());
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (isLoaction() ? 1 : 0);
        result = 31 * result + (getRelatedSensorsDelta() != null ? getRelatedSensorsDelta().hashCode() : 0);
        result = 31 * result + (getRelatedSensorsWhenWorkOfflineDelta() != null ? getRelatedSensorsWhenWorkOfflineDelta().hashCode() : 0);
        result = 31 * result + (activeTicks != null ? activeTicks.hashCode() : 0);
        temp = Double.doubleToLongBits(getCachedSensorState());
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
