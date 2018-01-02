package FinalProject.BL.Agents;

import FinalProject.BL.Problems.Actuator;
import FinalProject.BL.Problems.Prefix;
import FinalProject.BL.Problems.RelationType;
import FinalProject.BL.Problems.Sensor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public double getCachedSensorState() {
        return cachedSensorState;
    }

    private double cachedSensorState;

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

    public void calcAndUpdateCurrState(double minVal, double targetTickToCount, double[] powerConsumption, boolean isFromStart) {

        double currState = sensor.getCurrentState();
        double newState;
        if (isFromStart)
            newState = currState +  ((targetTick - targetTickToCount) * deltaWhenWorkOffline);
        else
            newState = currState + ((targetTickToCount - targetTick) * deltaWhenWorkOffline);

        if (newState == currState)
        {
            return ;
        }
        else{
            int i, counter;
            if (isFromStart)
            {   //because AFTER we include the hour. so the count before we'll not include it.

                i=0;
                double target = prefix.equals(Prefix.BEFORE) ? targetTick : targetTick-1;
                counter = (int) target;
            }
            else{
                double target = prefix.equals(Prefix.BEFORE) ? targetTick-1 : targetTick;
                i = (int) target;
                counter = (int) targetTickToCount;
            }
            for ( ; i<= counter; ++i)
            {
                if (currState < minVal)
                {
                    //lets go back tick before the change.
                    i--;
                    currState -=deltaWhenWorkOffline;
                    //now lets charge it to the maximum point
                    double ticksToCharge = Math.ceil((max - currState) / deltaWhenWork);
                    currState = updateValueToSensor(powerConsumption, currState, ticksToCharge, i, isFromStart);

                    i = (int)ticksToCharge + i + 1 ;

                }

                currState += deltaWhenWorkOffline;
            }

            //update the curr state now
            Map<Sensor, Double> toSend = new HashMap<>();
            toSend.put(sensor, currState);
            actuator.act(toSend);
        }
    }

    public double updateValueToSensor (double [] iterationPowerConsumption, double newState, double ticksToCharge, int idxTicks, boolean offlineWork)
    {
        for (int j=1; j<= ticksToCharge; ++j)
        {
            //update the powerCons array
            iterationPowerConsumption[j + idxTicks] = Double.sum(iterationPowerConsumption[j + idxTicks], powerConsumedInWork);
            newState = Double.sum(newState, deltaWhenWork);
            if(!offlineWork)
            this.activeTicks.add(j+idxTicks);
        }

        if (newState > max)
            newState = max;

        Map<Sensor, Double> toSend = new HashMap<>();
        toSend.put(sensor, newState);
        actuator.act(toSend);

        return newState;
    }





}
