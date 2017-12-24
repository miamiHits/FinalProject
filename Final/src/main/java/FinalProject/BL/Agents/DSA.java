package FinalProject.BL.Agents;
import FinalProject.BL.IterationData.AgentIterationData;
import FinalProject.BL.Problems.*;
import jade.lang.acl.ACLMessage;
import jade.core.AID;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class DSA extends SmartHomeAgentBehaviour {

    private boolean finished = false;
    private int currentNumberOfIter;
    public static final int START_TICK = 0;
    public final int FINAL_TICK = agent.getAgentData().getBackgroundLoad().length;
    private List<PropertyWithData> allProperties = new ArrayList<>();
    private  Map<Actuator, List<Integer>> DeviceToTicks = new HashMap<>();
    private static AgentIterationData agentIterationData;


    public DSA(String agentName, SmartHomeAgent agent)
    {
        this.agentName = agentName;
        this.agent = agent;
        this.currentNumberOfIter =0;
    }
    @Override
    protected void doIteration() {
        if (agent.isZEROIteration())
        {
            buildScheduleFromScratch();
            agent.setZEROIteration(false);
        }
        else
        {
            List<ACLMessage> messageList ;
            tryBuildSchedule();
        }

    }

    private void tryBuildSchedule() {
    }

    private void buildScheduleFromScratch() {
        //classifying the rules by activitness, start creating the prop object
        List <Rule> passiveRules = new ArrayList<>();
        List <Rule> activeRules = new ArrayList<>();
        for (Rule rule : agent.getAgentData().getRules())
        {
            if (rule.isActive())
                activeRules.add(rule);
            else
                passiveRules.add(rule);
        }

        passiveRules.forEach(pRule -> buildPropObj(pRule, true));
        activeRules.forEach(pRule -> buildPropObj(pRule, false));

        SetActuatorsAndSensors();
        double[] powerConsumption = checkHowLongDeviceNeedToWork();
        double price = calcPrice (powerConsumption);
        agentIterationData = new AgentIterationData(currentNumberOfIter, agent.getName(),price, powerConsumption);
        agent.setCurrIteration(agentIterationData);

        //TODO: Update the best iteration.
    }

    private double calcPrice(double[] powerConsumption) {
        double res = 0 ;
        double [] backgroundLoad = agent.getAgentData().getBackgroundLoad();
        double [] priceScheme = agent.getAgentData().getPriceScheme();
        for (int i=0 ; i<backgroundLoad.length; ++i)
        {
           Double.sum(res, Double.sum(backgroundLoad[i], Double.sum(powerConsumption[i], priceScheme[i])));
        }
        return res;
    }

    private void SetActuatorsAndSensors()
    {
        //add to the prop Objects their related actuators and sensors
        List<Actuator> notFound = new ArrayList<>();
        for(Actuator actuator : agent.getAgentData().getActuators())
        {
            for(Rule rule : agent.getAgentData().getRules())
            {
                //device was found in the rule
                if (rule.getDevice().getName().equals(actuator.getName()))
                {
                    //get the relevant prop object.
                    PropertyWithData prop = allProperties.stream()
                            .filter(x->x.name.equals(rule.getProperty()))
                            .findFirst().get();

                    //update the actuator
                    prop.actuator = actuator;
                    prop.isLoaction = false;

                    //update the deltot and sensors.
                    for(Action act : actuator.getActions())
                    {
                        matchSensors(act, prop, act.getName().equals("off")? true : false);
                    }
                }
                else
                {
                    notFound.add(actuator);
                }
            }
        }
        //interleave between the left actuators and the location
        for (Actuator actuator : notFound)
        {
            for(Action act : actuator.getActions())
            {
                for(PropertyWithData prop : allProperties.stream()
                                                .filter(p->p.isLoaction==true)
                                                .collect(Collectors.toList()))
                {
                    if (act.getEffects().stream()
                            .anyMatch(e->e.getProperty().equals(prop.name)))
                    {
                        prop.actuator = actuator;
                    }
                }

            }
        }
        //fill the sensors.
        for(PropertyWithData prop : allProperties.stream()
                .filter(p->p.isLoaction==true)
                .collect(Collectors.toList()))
        {
            for(Action act : prop.actuator.getActions())
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
                if (effect.getProperty().equals(prop.name))
                {
                    prop.deltaWhenWorkOffline = effect.getDelta();
                    prop.sensor = sensors.stream()
                            .filter(x->x.getSensingProperties().contains(prop.name))
                            .findFirst().get();
                }
                else
                {
                    prop.relatedSensorsWhenWorkOfflineDelta.put(effect.getProperty(),effect.getDelta());
                }
            }
            else
            {
                if (effect.getProperty().equals(prop.name)) {
                    prop.deltaWhenWork = effect.getDelta();
                } else {
                    prop.relatedSensorsDelta.put(effect.getProperty(), effect.getDelta());
                }
            }
        }


    }

    private void buildPropObj(Rule rule, boolean isPassive) {
        PropertyWithData prop = allProperties.stream()
                .filter(x->x.name.equals(rule.getProperty())).findFirst().get();
        if (prop == null)
        {
            prop = new PropertyWithData();
            prop.name = rule.getProperty();
            allProperties.add(prop);
        }

        if (isPassive)
        {
            prop.isPassiveOnly = true;
            switch (rule.getPrefixType())
            {
                case EQ:
                    prop.min = rule.getRuleValue();
                    break;
                case GEQ:
                    prop.max = rule.getRuleValue();
                    break;
                case LEQ:
                    prop.min = rule.getRuleValue();
                    break;
                case GT:
                    prop.max = rule.getRuleValue();
                    break;
                case LT:
                    prop.min = rule.getRuleValue();
                    break;
            }
        }
        else
        {
            prop.prefix = rule.getPrefix();
            prop.rt = rule.getPrefixType();
            prop.targetTick = rule.getRelationValue();
            prop.targetValue = rule.getRuleValue();
        }
    }

    private double[] checkHowLongDeviceNeedToWork()
    {
        double[] powerConsumption = new double[FINAL_TICK];
        for(PropertyWithData prop : allProperties.stream()
                .filter(p->p.isPassiveOnly==false)
                .collect(Collectors.toList()))
        {
            // first we'll get the target value and till when needed to be happened.
            double currentState = prop.sensor.getCurrentState();
            double ticksToWork=0;
            switch (prop.rt)
            {
                case EQ:
                case GEQ: //want to take here the lower bound, to work less that I can
                    ticksToWork = Math.ceil((prop.targetValue - currentState) / prop.deltaWhenWork);
                    break;
                case GT:
                    ticksToWork = Math.ceil((prop.targetValue+1 - currentState) / prop.deltaWhenWork);
                case LT:
                case LEQ:
                    ticksToWork = Math.ceil((prop.targetValue-1 - currentState) / prop.deltaWhenWork);
                    break;
            }
            prop.powerConsumption = ticksToWork * prop.deltaWhenWork;
            //draw ticks to work
            List<Integer> myTicks = new ArrayList<>();
            boolean flag = false;

            while (flag)
            {   //new iteration, flag starting with false as everything is okay.
                flag= false;
                int randomNum=0;
                for(int i=0; i<ticksToWork; ++i)
                {
                    switch (prop.prefix)
                    {
                        case BEFORE:    // +1 is from the formula.
                            randomNum = ThreadLocalRandom.current().nextInt(START_TICK, (int) (prop.targetTick + 1));
                            break;
                        case AFTER:
                            randomNum = ThreadLocalRandom.current().nextInt((int) (prop.targetTick), FINAL_TICK +1);
                            break;
                        case AT:
                            randomNum = (int) prop.targetTick;
                            break;
                    }

                    if (!myTicks.contains(randomNum))
                    {
                        myTicks.add(randomNum);
                    }
                }

                //there are sensors that reflect from this work! check if there is a problem with that.
                if (!prop.relatedSensorsDelta.isEmpty())
                {
                    for (String propName : prop.relatedSensorsDelta.keySet())
                    {
                        PropertyWithData relatedSensor = allProperties.stream()
                                .filter(x->x.name.equals(propName)).findFirst().get();
                        if (!relatedSensor.canBeModified(prop.relatedSensorsDelta.get(propName)))
                        {
                            //there is a problem with working at that hour, lets draw new tick.
                            flag = true;
                            break;
                        }
                    }
                }
            }

            List<Sensor> relevantSensors = new ArrayList<>();
            //adding to power consumption array, update the relevant sensors.
            for (int tick : myTicks)
            {
                powerConsumption[tick] += prop.deltaWhenWork;
                relevantSensors.add(prop.sensor);
                prop.relatedSensorsDelta.forEach((key, value) ->
                        Double.sum(powerConsumption[tick], value));
            }
            //update the state of the sensors
            prop.actuator.act(relevantSensors);
            // for debug propuse.
            DeviceToTicks.put(prop.actuator, myTicks);
        }


        return powerConsumption;
    }

    @Override
    protected void sendIterationToCollector() {
        ACLMessage aclmsg = new ACLMessage(ACLMessage.REQUEST);
        agent.getAgentData().getNeighbors().stream()
                .map(neighbor -> new AID(neighbor.getName(), AID.ISLOCALNAME)).forEach(aclmsg::addReceiver);

        try {
            aclmsg.setContentObject(agentIterationData);
            agent.send(aclmsg);
        } catch (IOException e) {
            agent.printLog(e.getMessage());
        }
    }

    @Override
    public void action() {
        this.agent.printLog("Starting work on Iteration: " + this.currentNumberOfIter);
        doIteration();
        sendIterationToCollector();
        this.currentNumberOfIter ++;

    }

    @Override
    public boolean done() {
        return finished;
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T,Object> keyExtractor) {
        Map<Object,String> seen = new ConcurrentHashMap<>();
        return t -> seen.put(keyExtractor.apply(t), "") == null;
    }

    private class PropertyWithData
    {
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
        private double deltaWhenWorkOffline;
        private double powerConsumption;
        private boolean isLoaction = true;
        private  Map<String,Double> relatedSensorsDelta = new HashMap<>();
        private  Map<String,Double> relatedSensorsWhenWorkOfflineDelta = new HashMap<>();


        public PropertyWithData () {}

        public boolean canBeModified (double amountOfChange)
        {
            double newState = sensor.getCurrentState() + amountOfChange;
            if (!Double.isNaN(max) && newState > max || (!Double.isNaN(min) && newState > min))
                return false;

            return true;
        }


    }
}
