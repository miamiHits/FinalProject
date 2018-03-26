package FinalProject.BL.Agents;

import FinalProject.BL.IterationData.AgentIterationData;
import FinalProject.BL.DataObjects.*;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

import static FinalProject.BL.DataCollection.PowerConsumptionUtils.*;

public class AlgorithmDataHelper
{
    public double totalPriceConsumption = Double.MAX_VALUE;
    public double ePeak = 0;
    private  Map<Actuator, List<Integer>> deviceToTicks = new HashMap<>();
    private List<PropertyWithData> allProperties;
    private SmartHomeAgent agent;
    private List<double[]> neighboursPriceConsumption = new ArrayList<>();
    private final static Logger logger = Logger.getLogger(AlgorithmDataHelper.class);

    public AlgorithmDataHelper (SmartHomeAgent agent)
    {
        this.agent = agent;
        allProperties = new ArrayList<>();
    }

    public AlgorithmDataHelper(AlgorithmDataHelper other) {
        this.totalPriceConsumption = other.totalPriceConsumption;
        this.allProperties = new ArrayList<>(other.allProperties);
        this.ePeak = other.ePeak;
        this.deviceToTicks = new HashMap<>(other.deviceToTicks);
        this.agent = new SmartHomeAgent(other.agent);
        this.neighboursPriceConsumption = new ArrayList<>(other.neighboursPriceConsumption);
    }

    public void buildNewPropertyData(Rule rule, boolean isPassive) {
        PropertyWithData prop = null;
        if (allProperties.size() > 0)
        {
            prop = allProperties.stream().filter(p -> p.getName().equals(rule.getProperty()))
                    .findFirst().orElse(null);
        }
        if (prop == null)
        {
            prop = new PropertyWithData();
            prop.setName(rule.getProperty());
            allProperties.add(prop);
        }
        if (isPassive)
        {
            switch (rule.getPrefixType())
            {
                case EQ:
                    prop.setMin(rule.getRuleValue());
                    break;
                case GEQ:
                    prop.setMin(rule.getRuleValue());
                    break;
                case LEQ:
                    prop.setMax(rule.getRuleValue());
                    break;
                case GT:
                    prop.setMin(rule.getRuleValue()+1);
                    break;
                case LT:
                    prop.setMax(rule.getRuleValue()-1);
                    break;
            }
        }
        else //is active
        {
            prop.setPrefix(rule.getPrefix());
            prop.setRt(rule.getPrefixType());
            prop.setTargetTick(rule.getRelationValue());

            switch (rule.getPrefixType())
            {
                case EQ:
                case GEQ:
                case LEQ:
                    prop.setTargetValue(rule.getRuleValue());
                    break;
                case GT:
                    prop.setTargetValue(rule.getRuleValue()+1);
                    break;
                case LT:
                    prop.setTargetValue(rule.getRuleValue()-1);
                    break;
            }

        }
    }

    public void SetActuatorsAndSensors()
    {
        ////<propName, Act>
        Map <String, Actuator> map = new HashMap<>();
        //iterate over the rules list.
        //Check if the device was found in the rules
        List<Actuator> actuators = agent.getAgentData().getActuators();
        for (Actuator a : actuators) {
            agent.getAgentData().getRules().stream().filter(r -> r.getDevice() != null)
                    .filter(r -> r.getDevice().getName().equals(a.getName()))
                    .findFirst().ifPresent(r -> map.put(r.getProperty(), a));
        }

        for(Map.Entry<String, Actuator> entry : map.entrySet())
        {
            //get the relevant prop object.
            PropertyWithData prop = allProperties.stream()
                    .filter(x->x.getName().equals(entry.getKey()))
                    .findFirst().get();

            //update the actuator
            prop.setActuator(entry.getValue());
            prop.setLoaction(false);

            //update the deltot and sensors.
            for(Action act : entry.getValue().getActions())
            {
                matchSensors(act, prop, act.getName().equals("off")? true : false);
            }
        }

        //Added all the Actuators that we did npt found in the rules (cause they are locations)
        List<Actuator> notFound = agent.getAgentData().getActuators().stream()
                .filter(a -> !map.containsValue(a)).collect(Collectors.toList());


        //interleave between the left actuators and the location
        for (Actuator actuator : notFound) {
            outerloop:
            for (Action act : actuator.getActions()) {
                for (PropertyWithData prop : allProperties.stream()
                        .filter(p -> p.isLoaction() == true)
                        .collect(Collectors.toList())) {
                    if (act.getEffects().stream()
                            .anyMatch(e -> e.getProperty().equals(prop.getName()))) {
                        prop.setActuator(actuator);
                        break outerloop;
                    }
                }

            }
        }

        //There are a props, that another prop got already it's actuator.
        allProperties.stream()
                .filter(p -> p.getActuator() == null)
                .collect(Collectors.toList()).forEach(prop -> {
            actuators.forEach(actuator -> {
                for (Action act : actuator.getActions()) {
                    for (Effect effect : act.getEffects()) {
                        if (effect.getProperty().equals(prop.getName())) {
                            prop.setActuator(actuator);
                            break;
                        }
                    }
                }
            });
        });
        //fill the sensors.
        for(PropertyWithData prop : allProperties.stream()
                .filter(p->p.isLoaction()==true)
                .collect(Collectors.toList())) {
            for (Action act : prop.getActuator().getActions()) {
                matchSensors(act, prop, act.getName().equals("off") ? true : false);

            }
        }
    }

    private void matchSensors(Action act, PropertyWithData prop, boolean isOffline)
    {
        double granularity = agent.getAgentData().getGranularity();
        List<Sensor> sensors = agent.getAgentData().getSensors();
        if (!(act.getEffects().stream().anyMatch(x->x.getDelta() < 0 && x.getProperty().equals(prop.getName())))  || isOffline) {
            for (Effect effect : act.getEffects()) {
                if (isOffline) {
                    //match the actual sensor
                    if (effect.getProperty().equals(prop.getName())) {
                        if (granularity!=60 && effect.getDelta()>0) {calcNewDelta(granularity, effect);}
                        prop.setDeltaWhenWorkOffline(effect.getDelta());
                        prop.setSensor(sensors.stream()
                                .filter(x -> x.getSensingProperties().contains(prop.getName()))
                                .findFirst().get());
                        prop.setCachedSensor(sensors.stream()
                                .filter(x -> x.getSensingProperties().contains(prop.getName()))
                                .findFirst().get().getCurrentState());
                    } else {
                        //match another sensors that work in this time
                        prop.relatedSensorsWhenWorkOfflineDelta.put(effect.getProperty(), effect.getDelta());
                    }
                } else {
                    // GT 0 for the case there is prop that go down bc another prop. FE: charge go down if clean work
                    if (granularity!=60) {calcNewDelta(granularity, effect);}
                    if (effect.getProperty().equals(prop.getName())) {
                        if (effect.getDelta() > 0) {
                            prop.setDeltaWhenWork(effect.getDelta());
                            prop.setPowerConsumedInWork(act.getPowerConsumption());

                        }
                    } else {
                        if (!prop.relatedSensorsDelta.containsKey(effect.getProperty())){
                            prop.relatedSensorsDelta.put(effect.getProperty(), effect.getDelta());
                        }
                    }
                }
            }
        } else {
            return;
        }
    }

    private void calcNewDelta(double granularity, Effect effect) {
        double relation = granularity / 60;
        effect.setDelta(effect.getDelta()/relation);

    }

    private void getSubsets(List<Integer> superSet, int k, int idx, Set<Integer> current,List<Set<Integer>> solution) {
        //successful stop clause
        if (current.size() == k) {
            solution.add(new HashSet<>(current));
            return;
        }
        //unseccessful stop clause
        if (idx == superSet.size()) return;
        Integer x = superSet.get(idx);
        current.add(x);
        //"guess" x is in the subset
        getSubsets(superSet, k, idx+1, current, solution);
        current.remove(x);
        //"guess" x is not in the subset
        getSubsets(superSet, k, idx+1, current, solution);
    }

    public List<Set<Integer>> getSubsets(List<Integer> superSet, int k) {
        List<Set<Integer>> res = new ArrayList<>();
        getSubsets(superSet, k, 0, new HashSet<>(), res);
        return res;
    }

    public void calcPowerConsumptionForAllNeighbours() {
        neighboursPriceConsumption.clear();
        logger.info("Saving all my neighbors sched - stage 1");
        List<AgentIterationData> myNeighborsShed = agent.getMyNeighborsShed();
        for (AgentIterationData agentData : myNeighborsShed) {
            double [] neighbourConsumption = cloneArray(agentData.getPowerConsumptionPerTick());
            neighboursPriceConsumption.add(neighbourConsumption);
        }
    }

    public double calcHowLongDeviceNeedToWork(PropertyWithData prop) {
        double ticksToWork =0;
        // first we'll get the target value and till when needed to be happened.
        double currentState = prop.getSensor().getCurrentState();
        switch (prop.getRt())
        {
            case EQ:
            case GEQ: //want to take here the lower bound, to work less that I can
                ticksToWork = Math.ceil((prop.getTargetValue() - currentState) / prop.getDeltaWhenWork());
                break;
            case GT:
                ticksToWork = Math.ceil((prop.getTargetValue()+1 - currentState) / prop.getDeltaWhenWork());
                break;
            case LT:
                ticksToWork = Math.ceil((prop.getTargetValue()-1 - currentState) / prop.getDeltaWhenWork());
                if (((ticksToWork *  prop.getDeltaWhenWork()) + currentState >= prop.getTargetValue()) )
                {
                    ticksToWork--;
                }
                break;
            case LEQ:
                ticksToWork = Math.ceil((prop.getTargetValue()-1 - currentState) / prop.getDeltaWhenWork());
                break;
        }

        return ticksToWork;
    }

    public List<PropertyWithData> getAllProperties() {
        return allProperties;
    }

    public Map<Actuator, List<Integer>> getDeviceToTicks() {
        return deviceToTicks;
    }

    public List<double[]> getNeighboursPriceConsumption() {
        return neighboursPriceConsumption;
    }

    public void calcAndSetTotalPowerConsumption(double cSum) {
        this.totalPriceConsumption = calcTotalPowerConsumption(cSum);
        logger.info("TOTAL power consumption is : " + this.totalPriceConsumption);

    }

    public double calcTotalPowerConsumption(double cSum, double[] myPowerConsumption) {
        logger.info("Calculating total power consumption - stage 2");

        List<double[]> scheds = new ArrayList<>(this.neighboursPriceConsumption);
//        double [] myPowerCons = cloneArray(agent.getCurrIteration().getPowerConsumptionPerTick());
        scheds.add(myPowerConsumption);
        this.ePeak = calculateEPeak(scheds);
        return getAC() * cSum + getAE() * ePeak;
    }

    //TODO taken from static class
    public double calculateEPeak(List<double[]> schedules) {
        double eSqrSum = 0;
        for (double[] sched : schedules) {
            for (double aSched : sched) {
                eSqrSum += Math.pow(aSched, 2);
            }
        }
        return eSqrSum * getAE();
    }

    public double calcTotalPowerConsumption(double cSum) {
        double [] myPowerCons = cloneArray(agent.getCurrIteration().getPowerConsumptionPerTick());
        return calcTotalPowerConsumption(cSum, myPowerCons);
    }

    public void correctEpeak(double[] improvedSched, double[] prevSched) {
        for (double tick : improvedSched) {
            ePeak += Math.pow(tick, 2);
        }
        for (double tick : prevSched) {
            ePeak -= Math.pow(tick, 2);
        }
    }

    public void checkForPassiveRules() {
        for (PropertyWithData prop : allProperties)
        {
            if (prop.getRt()==null)
            {
                prop.setPassiveOnly(true);
            }
        }
    }

    public double[] cloneArray(double[] old)
    {
        double[] newList = Arrays.copyOf(old, old.length);

        return newList;
    }

    public List<Integer> cloneList(List<Integer> old)
    {
        List<Integer> newList = new ArrayList<>();
        newList.addAll(old);
        return newList;
    }

    public void resetProperties() {
        for (PropertyWithData prop : this.allProperties)
            prop.getSensor().setCurrentState(prop.getCachedSensorState());
    }

}
