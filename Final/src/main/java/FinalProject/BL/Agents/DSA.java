package FinalProject.BL.Agents;
import FinalProject.BL.IterationData.AgentIterationData;
import FinalProject.BL.Problems.*;
import FinalProject.DAL.AlgorithmLoader;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import org.apache.log4j.Logger;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class DSA extends SmartHomeAgentBehaviour {

    private boolean finished = false;
    private int currentNumberOfIter;
    public static final int START_TICK = 0;
    public int FINAL_TICK;
    public List<PropertyWithData> allProperties = new ArrayList<>();
    public  Map<Actuator, List<Integer>> DeviceToTicks = new HashMap<>();
    public static AgentIterationData agentIterationData;
    private double totalPriceConsumption=0;
    private final static Logger logger = Logger.getLogger(AlgorithmLoader.class);


    public DSA(SmartHomeAgent agent)
    {
        this.agent = agent;
        this.currentNumberOfIter =0;
        this.FINAL_TICK = agent.getAgentData().getBackgroundLoad().length -1;
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
            List<ACLMessage> messageList = waitForNeighbourMessages();
            parseMessages(messageList);
            tryBuildSchedule();
        }

    }

    private void parseMessages(List<ACLMessage> messageList) {
        //TODO: Recognized Aviv message.
        List<Serializable> neighbors = new ArrayList<>();
        for (int i=0; i< messageList.size(); ++i)
        {
            try {
                neighbors.add(messageList.get(i).getContentObject());
            } catch (UnreadableException e) {
                e.printStackTrace();
            }
        }
    }

    //a blocking method that waits far receiving messages(without filtration) from all neighbours and data collector
    private List<ACLMessage> waitForNeighbourMessages() {
        List<ACLMessage> messages = new ArrayList<>();
        ACLMessage receivedMessage;
        int neighbourCount = this.agent.getAgentData().getNeighbors().size();
        //TODO wait also for DATA COLLECTOR Message
        while (messages.size() <= neighbourCount + 1)//the additional one is for the data collector's message
        {
            receivedMessage = this.agent.blockingReceive();
            messages.add(receivedMessage);
        }
        return messages;
    }

    private void tryBuildSchedule() {

    }

    public boolean buildScheduleFromScratch() {
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
        return true;
    }

    private double calcPrice(double[] powerConsumption) {
        double res = 0 ;
        double [] backgroundLoad = agent.getAgentData().getBackgroundLoad();
        double [] priceScheme = agent.getAgentData().getPriceScheme();
        for (int i=0 ; i<backgroundLoad.length; ++i)
        {
           double temp =  Double.sum(powerConsumption[i], priceScheme[i]);
           double temp2 = Double.sum(temp,backgroundLoad[i] );
           res = Double.sum(temp2, res);
          // Double.sum(res, Double.sum(backgroundLoad[i], Double.sum(powerConsumption[i], priceScheme[i])));
        }
        return res;
    }

    private void SetActuatorsAndSensors()
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
                            .filter(x->x.name.equals(entry.getKey()))
                            .findFirst().get();

            //update the actuator
            prop.actuator = entry.getValue();
            prop.isLoaction = false;

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
                                                .filter(p->p.isLoaction==true)
                                                .collect(Collectors.toList()))
                {
                    if (act.getEffects().stream()
                            .anyMatch(e->e.getProperty().equals(prop.name)))
                    {
                        prop.actuator = actuator;
                        break;
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
        PropertyWithData prop= null;
        if (allProperties.size() > 0)
        {
            prop = allProperties.stream().filter(p -> p.name.equals(rule.getProperty()))
                    .findFirst().orElse(null);
        }
        if (prop == null)
        {
            prop = new PropertyWithData();
            prop.name = rule.getProperty();
            allProperties.add(prop);
        }
        if (isPassive)
        {
            switch (rule.getPrefixType())
            {
                case EQ:
                    prop.min = rule.getRuleValue();
                    break;
                case GEQ:
                    prop.min = rule.getRuleValue();
                    break;
                case LEQ:
                    prop.max = rule.getRuleValue();
                    break;
                case GT:
                    prop.min = rule.getRuleValue();
                    break;
                case LT:
                    prop.max = rule.getRuleValue();
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
        double[] powerConsumption = new double[FINAL_TICK+1];
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
            boolean flag = true;

            while (flag)
            {   //new iteration, flag starting with false as everything is okay.
                flag= false;
                int randomNum=0;
                for(int i=0; i<ticksToWork; ++i)
                {
                    switch (prop.prefix)
                    {
                        case BEFORE:    // Min + (int)(Math.random() * ((Max - Min) + 1))
                            randomNum = START_TICK + (int)(Math.random() * ((prop.targetTick - START_TICK) + 1));
                            break;
                        case AFTER:
                            randomNum = (int) (prop.targetTick + (int)(Math.random() * ((FINAL_TICK -  prop.targetTick) + 1)));
                            break;
                        case AT:
                            randomNum = (int) prop.targetTick;
                            break;
                    }

                    if (!myTicks.contains(randomNum))
                    {
                        myTicks.add(randomNum);
                    }
                    else{
                        --i;
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
                if (!relevantSensors.contains(prop.sensor)) relevantSensors.add(prop.sensor);
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
