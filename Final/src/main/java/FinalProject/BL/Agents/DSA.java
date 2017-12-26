package FinalProject.BL.Agents;
import FinalProject.BL.IterationData.AgentIterationData;
import FinalProject.BL.Problems.*;
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
    public static AgentIterationData agentIterationData;
    private final static Logger logger = Logger.getLogger(DSA.class);
    private AlgorithmDataHelper helper;

    public DSA(SmartHomeAgent agent)
    {
        this.agent = agent;
        this.currentNumberOfIter =0;
        this.FINAL_TICK = agent.getAgentData().getBackgroundLoad().length -1;
        this.helper = new AlgorithmDataHelper(agent);
    }
    @Override
    protected void doIteration() {
        if (agent.isZEROIteration())
        {
            logger.info("Starting build schedule");
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

        agent.setMyNeighborsShed(neighbors);
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

        boolean buildNewSched = helper.drawCoin() == 1 ? true : false;
        if (buildNewSched)
        {
            for (Actuator act : helper.DeviceToTicks.keySet())
            {
                List<Integer> newProposeTicks = helper.calcNewTicks(act);
                helper.DeviceToTicks.put(act, newProposeTicks);
                double[] powerConsumption = buildNewScheduleAccordingToNewTicks();
                double price = helper.calcPrice(powerConsumption);
                helper.totalPriceConsumption = price;
                agentIterationData = new AgentIterationData(currentNumberOfIter, agent.getName(),price, powerConsumption);
                agent.setCurrIteration(agentIterationData);

                //TODO: Update the best iteration.
            }
        }
        else{
            return;
        }

    }

    private double[] buildNewScheduleAccordingToNewTicks() {
        double[] powerConsumption = new double[FINAL_TICK+1];



        return powerConsumption;
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

        passiveRules.forEach(pRule -> helper.buildPropObj(pRule, true));
        activeRules.forEach(pRule -> helper.buildPropObj(pRule, false));

        helper.SetActuatorsAndSensors();
        double[] powerConsumption = tryBuildScheduleIterationZero();
        double price = helper.calcPrice (powerConsumption);
        helper.totalPriceConsumption = price;
        agentIterationData = new AgentIterationData(currentNumberOfIter, agent.getName(),price, powerConsumption);
        agent.setCurrIteration(agentIterationData);

        //TODO: Update the best iteration.
        return true;
    }

    private double[] tryBuildScheduleIterationZero()
    {
        double[] powerConsumption = new double[FINAL_TICK+1];
        for(AlgorithmDataHelper.PropertyWithData prop : helper.allProperties.stream()
                .filter(p->p.isPassiveOnly()==false)
                .collect(Collectors.toList()))
        {
            // first we'll get the target value and till when needed to be happened.
            double currentState = prop.getSensor().getCurrentState();
            double ticksToWork=0;
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
            //draw ticks to work
            List<Integer> myTicks = new ArrayList<>();
            boolean flag = true;

            while (flag)
            {   //new iteration, flag starting with false as everything is okay.
                flag= false;
                int randomNum=0;
                for(int i=0; i<ticksToWork; ++i)
                {
                    switch (prop.getPrefix())
                    {
                        case BEFORE:    // Min + (int)(Math.random() * ((Max - Min) + 1))
                            randomNum = START_TICK + (int)(Math.random() * ((prop.getTargetTick() - START_TICK) + 1));
                            break;
                        case AFTER:
                            randomNum = (int) (prop.getTargetTick() + (int)(Math.random() * ((FINAL_TICK -  prop.getTargetTick()) + 1)));
                            break;
                        case AT:
                            randomNum = (int) prop.getTargetTick();
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
                        AlgorithmDataHelper.PropertyWithData relatedSensor = helper.allProperties.stream()
                                .filter(x->x.getName().equals(propName)).findFirst().get();
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
                powerConsumption[tick] += prop.getDeltaWhenWork();
                if (!relevantSensors.contains(prop.getSensor())) relevantSensors.add(prop.getSensor());
                prop.relatedSensorsDelta.forEach((key, value) ->
                        Double.sum(powerConsumption[tick], value));
            }
            //update the state of the sensors
            prop.getActuator().act(relevantSensors);
            // for debug propuse.
            helper.DeviceToTicks.put(prop.getActuator(), myTicks);
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

    public void setHelper(AlgorithmDataHelper helper) {
        this.helper = helper;
    }

    public AlgorithmDataHelper getHelper() {
        return helper;
    }
}
