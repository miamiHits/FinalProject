package FinalProject.BL.Agents;

import FinalProject.BL.IterationData.AgentIterationData;
import FinalProject.BL.Problems.*;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static FinalProject.BL.DataCollection.PowerConsumptionUtils.calculateTotalConsumptionWithPenalty;

public class AlgorithmDataHelper
{
    public double totalPriceConsumption=0;
    private  Map<Actuator, List<Integer>> DeviceToTicks = new HashMap<>();
    private List<PropertyWithData> allProperties;
    private SmartHomeAgent agent;
    private double[] neighboursTotals = new double[agent.getAgentData().getBackgroundLoad().length];
    private List<double[]> neighboursPriceConsumption = new ArrayList<>();
    private List<Integer> rushTicks = new ArrayList<>();
    private double averageConsumption;
    private final static Logger logger = Logger.getLogger(AlgorithmDataHelper.class);

    public AlgorithmDataHelper (SmartHomeAgent agent)
    {
        this.agent = agent;
        allProperties = new ArrayList<>();
    }

    public PropertyWithData createNewProp()
    {
        return  new PropertyWithData();
    }

    public void buildNewPropertyData(Rule rule, boolean isPassive) {
        PropertyWithData prop= null;
        if (allProperties.size() > 0)
        {
            prop = allProperties.stream().filter(p -> p.getName().equals(rule.getProperty()))
                    .findFirst().orElse(null);
        }
        if (prop == null)
        {
            prop = createNewProp();
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
                    prop.setMin(rule.getRuleValue());
                    break;
                case LT:
                    prop.setMax(rule.getRuleValue());
                    break;
            }
        }
        else
        {
            prop.setPrefix(rule.getPrefix());
            prop.setRt(rule.getPrefixType());
            prop.setTargetTick(rule.getRelationValue());
            prop.setTargetValue(rule.getRuleValue());
        }
    }

    public void SetActuatorsAndSensors()
    {
        Map <String, Actuator> map = new HashMap<String, Actuator>();
        //iterate over the rules list.
        //Check if the device was found in the rules
        List<Actuator> actuators = agent.getAgentData().getActuators();
        for (int i = 0, actuatorsSize = actuators.size(); i < actuatorsSize; i++) {
            Actuator a = actuators.get(i);
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
        for (Actuator actuator : notFound)
        {
            for(Action act : actuator.getActions())
            {
                for(PropertyWithData prop : allProperties.stream()
                        .filter(p->p.isLoaction()==true)
                        .collect(Collectors.toList()))
                {
                    if (act.getEffects().stream()
                            .anyMatch(e->e.getProperty().equals(prop.getName())))
                    {
                        prop.setActuator(actuator);
                        break;
                    }
                }

            }
        }
        //fill the sensors.
        for(PropertyWithData prop : allProperties.stream()
                .filter(p->p.isLoaction()==true)
                .collect(Collectors.toList()))
        {
            for(Action act : prop.getActuator().getActions())
            {
                matchSensors(act, prop, act.getName().equals("off")? true : false);
            }
        }
    }

    private void matchSensors(Action act, PropertyWithData prop, boolean isOffline)
    {
        List<Sensor> sensors = agent.getAgentData().getSensors();
        for(Effect effect : act.getEffects())
        {
            if (isOffline)
            {
                if (effect.getProperty().equals(prop.getName()))
                {
                    prop.setDeltaWhenWorkOffline(effect.getDelta());
                    prop.setSensor(sensors.stream()
                            .filter(x->x.getSensingProperties().contains(prop.getName()))
                            .findFirst().get());
                }
                else
                {
                    prop.relatedSensorsWhenWorkOfflineDelta.put(effect.getProperty(),effect.getDelta());
                }
            }
            else
            {
                if (effect.getProperty().equals(prop.getName())) {
                    prop.setDeltaWhenWork(effect.getDelta());
                } else {
                    prop.relatedSensorsDelta.put(effect.getProperty(), effect.getDelta());
                }
            }
        }


    }



    public void solve(int[] a, int k, int i, List<List<Integer>> subsets) {
        if (i == a.length) {
            return;
        } else {
            // loop over all subsets and try to put a[i] in
            for (int j = 0; j < subsets.size(); j++) {
                if (subsets.get(j).size() < k) {
                    // subset j not full
                    subsets.get(j).add(a[i]);
                    solve(a, k, i+1, subsets); // do recursion
                    subsets.get(j).remove((Integer)a[i]);

                    if (subsets.get(j).size() == 0) {
                        // Not skipping empty subsets, so I won't get duplicates
                        break;
                    }
                }
            }
        }
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T,Object> keyExtractor) {
        Map<Object,String> seen = new ConcurrentHashMap<>();
        return t -> seen.put(keyExtractor.apply(t), "") == null;
    }

    public void calcPriceSchemeForAllNeighbours() {
        List<AgentIterationData> myNeighborsShed = agent.getMyNeighborsShed();
        //first sum all the neighbours.
        for (AgentIterationData agentData : myNeighborsShed)
        {
            double [] neighbourConsumption = agentData.getPowerConsumptionPerTick();
            neighboursPriceConsumption.add(neighbourConsumption);
            IntStream.range(0, neighbourConsumption.length)
                    .forEachOrdered(i -> neighboursTotals[i] += neighbourConsumption[i]);
        }

        //calc the average
        double sum = Arrays.stream(neighboursTotals, 0, neighboursTotals.length).sum();
        averageConsumption = sum / agent.getAgentData().getNeighbors().size();

        //sign the "rush" ticks.
        IntStream.range(0, neighboursTotals.length).
                filter(i -> neighboursTotals[i] > averageConsumption).forEachOrdered(i -> rushTicks.add(i));
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
            case LT:
            case LEQ:
                ticksToWork = Math.ceil((prop.getTargetValue()-1 - currentState) / prop.getDeltaWhenWork());
                break;
        }
        prop.setPowerConsumption(ticksToWork * prop.getDeltaWhenWork());

        return ticksToWork;
    }

    public void updateConsumption(PropertyWithData prop, List<Integer> myTicks, double[] powerConsumption) {
        List<Sensor> relevantSensors = new ArrayList<>();
        //adding to power consumption array, update the relevant sensors.
        for (int tick : myTicks)
        {
            powerConsumption[tick] += prop.getDeltaWhenWork();
            if (!relevantSensors.contains(prop.getSensor())) relevantSensors.add(prop.getSensor());
            prop.relatedSensorsDelta.forEach((key, value) ->
                    Double.sum(powerConsumption[tick], value));
        }
        //update the state of the sensors
        prop.getActuator().act(relevantSensors);
        // for debug propuse.
        DeviceToTicks.put(prop.getActuator(), myTicks);
    }

    public List<PropertyWithData> getAllProperties() {
        return allProperties;
    }

    public void setAllProperties(List<PropertyWithData> allProperties) {
        this.allProperties = allProperties;
    }

    public List<Integer> getRushTicks() {
        return rushTicks;
    }

    public void setRushTicks(List<Integer> rushTicks) {
        this.rushTicks = rushTicks;
    }

    public Map<Actuator, List<Integer>> getDeviceToTicks() {
        return DeviceToTicks;
    }

    public void setDeviceToTicks(Map<Actuator, List<Integer>> deviceToTicks) {
        DeviceToTicks = deviceToTicks;
    }

    public List<double[]> getNeighboursPriceConsumption() {
        return neighboursPriceConsumption;
    }

    public void setNeighboursPriceConsumption(List<double[]> neighboursPriceConsumption) {
        this.neighboursPriceConsumption = neighboursPriceConsumption;
    }
}
