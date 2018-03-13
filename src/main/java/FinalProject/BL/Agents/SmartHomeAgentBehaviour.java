package FinalProject.BL.Agents;


import FinalProject.BL.DataCollection.DataCollectionCommunicator;
import FinalProject.BL.DataObjects.Prefix;
import FinalProject.BL.DataObjects.Rule;
import FinalProject.BL.DataObjects.Sensor;
import FinalProject.BL.IterationData.AgentIterationData;
import FinalProject.BL.IterationData.IterationCollectedData;
import FinalProject.Utils;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import static FinalProject.BL.DataCollection.PowerConsumptionUtils.calculateTotalConsumptionWithPenalty;

public abstract class SmartHomeAgentBehaviour extends Behaviour implements Serializable{

    public static final int START_TICK = 0;
    private final Random randGenerator = new Random();
    public SmartHomeAgent agent;
    protected int currentNumberOfIter;
    protected int FINAL_TICK;
    protected AlgorithmDataHelper helper;
    protected AgentIterationData agentIterationData;
    protected IterationCollectedData agentIteraionCollected;

    private final static Logger logger = Logger.getLogger(SmartHomeAgentBehaviour.class);
    protected boolean finished = false;
    protected double[] iterationPowerConsumption;

    //Main method! implemented by inheriting algos!
    protected abstract void doIteration();

    @Override
    public void action() {
        logger.debug("action method invoked");
        doIteration();
        sendIterationToCollector();
        sendIterationToNeighbors();
    }

    protected void sendIterationToCollector()
    {
        logger.debug(String.format("%s sends its iteration to the data collector", this.agent.getAgentData().getName()));

        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(DataCollectionCommunicator.SERVICE_TYPE);

        template.addServices(sd);

        try {
            DFAgentDescription[] result = DFService.search(this.agent, template);
            if (result.length > 0)
            {
                logger.debug(String.format("found %d %s agents, this first one's AID is %s",
                        result.length,
                        DataCollectionCommunicator.SERVICE_TYPE,
                        result[0].getName().toString()));
                ACLMessage message = new ACLMessage(ACLMessage.REQUEST);//TODO gal reconsider the type
                message.setOntology(agent.getProblemId()+agent.getAlgoId());
                for (DFAgentDescription foundAID : result)
                {
                    message.addReceiver(foundAID.getName());
                }
                message.setContentObject(agentIteraionCollected);
                logger.debug(String.format("sending iteration #%d data to data collector", agentIteraionCollected.getIterNum()));
                agent.send(message);
            }
            else
            {
                logger.error("could not find the data communicator");//TODO gal decide how to handle such scenario);
            }
        }
        catch (FIPAException | IOException | NullPointerException e) {
            logger.error(e);
        }

    }

    protected void sendIterationToNeighbors()
    {
        logger.debug(String.format("%s sends its iteration to its neighbours", this.agent.getAgentData().getName()));

        ACLMessage aclmsg = new ACLMessage(ACLMessage.REQUEST);
        agent.getAgentData().getNeighbors().stream()
                .map(neighbor -> new AID(neighbor.getName(), AID.ISLOCALNAME))
                .forEach(aclmsg::addReceiver);

        try {
            aclmsg.setContentObject(agent.getCurrIteration());
            agent.send(aclmsg);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    protected void initHelper() {
        //classifying the rules by activeness, start creating the prop object
        List<Rule> passiveRules = new ArrayList<>();
        List <Rule> activeRules = new ArrayList<>();
        for (Rule rule : agent.getAgentData().getRules())
        {
            if (rule.isActive()) {
                activeRules.add(rule);
            }
            else {
                passiveRules.add(rule);
            }
        }

        passiveRules.forEach(pRule -> helper.buildNewPropertyData(pRule, true));
        activeRules.forEach(rRule -> helper.buildNewPropertyData(rRule, false));
        helper.checkForPassiveRules();
        helper.SetActuatorsAndSensors();
    }

    protected int drawRandomNum(int start, int last) {
        return start + (int) (Math.random() * ((last - start) + 1));
    }

    protected void updateTotals(PropertyWithData prop, List<Integer> myTicks, Map<String, Double> sensorsToCharge)
    {
        List<Integer> activeTicks = helper.cloneList(myTicks);
        helper.getDeviceToTicks().put(prop.getActuator(), activeTicks);
        for (int i = 0; i < myTicks.size(); ++i) {
            iterationPowerConsumption [myTicks.get(i)] = Double.sum(this.iterationPowerConsumption[myTicks.get(i)],
                    prop.getPowerConsumedInWork());
            if (!sensorsToCharge.isEmpty()) {
                for (Map.Entry<String,Double> entry : sensorsToCharge.entrySet()) {
                    PropertyWithData brother = helper.getAllProperties().stream().filter(x->x.getName().equals(entry.getKey())).findFirst().get();
                    double timeToCharge = (i + 1) % entry.getValue();
                    if (i == (int) timeToCharge) {
                        brother.updateValueToSensor(this.iterationPowerConsumption, brother.getMin(), entry.getValue(), i, true);
                    }
                }
            }
        }

        //update the sensor
        double currState = prop.getSensor().getCurrentState() + (prop.getDeltaWhenWork() * myTicks.size());
        if (currState > prop.getMax())
            currState = prop.getMax();
        Map<Sensor, Double> toSend = new HashMap<>();
        toSend.put(prop.getSensor(), currState);
        prop.getActuator().act(toSend);

    }

    protected void startWorkZERO(PropertyWithData prop, Map<String, Double> sensorsToCharge, double ticksToWork) {
        List<Integer> myTicks = new ArrayList<>();
        if (ticksToWork <= 0) {
            prop.calcAndUpdateCurrState(prop.getTargetValue(), FINAL_TICK, iterationPowerConsumption, false);
            List<Integer> activeTicks = helper.cloneList(prop.activeTicks);
            helper.getDeviceToTicks().put(prop.getActuator(), activeTicks);
        }
        else {
            int randomNum = 0;
            for (int i = 0; i < ticksToWork; ++i) {
                switch (prop.getPrefix()) {
                    case BEFORE:    // Min + (int)(Math.random() * ((Max - Min) + 1)). NOT INCLUDE THE HOUR
                        randomNum = START_TICK + (int) (Math.random() * (((prop.getTargetTick()-1) - START_TICK) + 1));
                        break;
                    case AFTER:
                        if (prop.getTargetTick() + ticksToWork > (this.iterationPowerConsumption.length)) {
                            double targetTick = prop.getTargetTick();
                            for (int j= 0 ; j< ticksToWork; j++) {
                                randomNum = drawRandomNum(0,(int) targetTick - j);
                                if (!myTicks.contains(randomNum))
                                    myTicks.add(randomNum);
                            }
                            i = (int)ticksToWork;
                        }
                        else {
                            randomNum = (int) (prop.getTargetTick() + (int) (Math.random() * ((FINAL_TICK - prop.getTargetTick()) + 1)));
                        }
                        break;
                    case AT:
                        if (ticksToWork == 1) {
                            myTicks.add((int)prop.getTargetTick());
                        }
                        else
                        {   double targetTick = prop.getTargetTick();
                            for (int j = 0 ; j< ticksToWork; j++) {
                                randomNum = drawRandomNum(0,(int)targetTick - j);
                                if (!myTicks.contains(randomNum)) {
                                    myTicks.add(randomNum);
                                }
                            }
                        }
                        break;
                }
                
                if (prop.getPrefix() == Prefix.AT) break;
                if (!myTicks.contains(randomNum)) {
                    myTicks.add(randomNum);
                }
                else {
                    i--;
                }
            }

            updateTotals(prop, myTicks, sensorsToCharge);
        }
    }

    protected boolean drawCoin() {
//        int[] notRandomNumbers = new int [] {0,0,0,0,1,1,1,1,1,1};
//        double idx = Math.floor(Math.random() * notRandomNumbers.length);
//        return notRandomNumbers[(int) idx];
        return randGenerator.nextBoolean();
    }

    protected List<Integer> calcRangeOfWork(PropertyWithData prop) {
        List<Integer> rangeForWork = new ArrayList<>();

        switch (prop.getPrefix())
        {
            case BEFORE: //NOT Include the hour
                for (int i=0; i< prop.getTargetTick(); ++i) {
                    rangeForWork.add(i);
                }
                break;
            case AFTER:
                for (int i= (int) prop.getTargetTick(); i < agent.getAgentData().getBackgroundLoad().length; ++i) {
                    rangeForWork.add(i);
                }
                break;
            case AT:
                rangeForWork.add((int) prop.getTargetTick());
                break;
        }

        return rangeForWork;
    }

    protected List<Integer> calcBestPrice(PropertyWithData prop, List<Set<Integer>> subsets)
    {
        double bestPrice = helper.totalPriceConsumption;
        List<Integer> newTicks = new ArrayList<>();
        double [] prevPowerConsumption = helper.cloneArray(agent.getCurrIteration().getPowerConsumptionPerTick());
        double [] newPowerConsumption = helper.cloneArray(agent.getCurrIteration().getPowerConsumptionPerTick());
        //get the specific tick this device work in
        List<Integer> prevTicks = helper.getDeviceToTicks().get(prop.getActuator());
        //remove them from the array
        for (Integer tick : prevTicks) {
            newPowerConsumption[tick] = newPowerConsumption[tick] -  prop.getPowerConsumedInWork();
        }

        double [] copyOfNew = helper.cloneArray(newPowerConsumption);
        boolean improved = false;

        //find the best option
        for(Set<Integer> ticks : subsets)
        {
            //Adding the ticks to array
            for (Integer tick : ticks) {
                double temp = newPowerConsumption[tick];
                newPowerConsumption[tick] = Double.sum(temp, prop.getPowerConsumedInWork());
            }
            double res = calculateTotalConsumptionWithPenalty(agent.getcSum(), newPowerConsumption, prevPowerConsumption
                    ,helper.getNeighboursPriceConsumption(), agent.getAgentData().getPriceScheme());

            if (res <= helper.totalPriceConsumption && res <= bestPrice) {
                bestPrice = res;
                newTicks.clear();
                newTicks.addAll(ticks);
                improved = true;
            }

            //goBack
            newPowerConsumption = helper.cloneArray(copyOfNew);
        }

        if(!improved) {
            newTicks = helper.getDeviceToTicks().get(prop.getActuator());
        }

        return newTicks;
    }

    public abstract SmartHomeAgentBehaviour cloneBehaviour();

    //a blocking method that waits far receiving messages(without filtration) from all neighbours and data collector
    protected List<ACLMessage> waitForNeighbourMessages() {
        List<ACLMessage> messages = new ArrayList<>();
        ACLMessage receivedMessage;
        int neighbourCount = this.agent.getAgentData().getNeighbors().size();
        while (messages.size() < neighbourCount)//the additional one is for the data collector's message
        {
            receivedMessage = this.agent.blockingReceive(SmartHomeAgent.MESSAGE_TEMPLATE_SENDER_IS_NEIGHBOUR);
            logger.debug(Utils.parseAgentName(this.agent) + " received a message from " + Utils.parseAgentName(receivedMessage.getSender()));
            messages.add(receivedMessage);
        }
        clearAmsMessages();
        // wait for the message from the collector
        receivedMessage = this.agent.blockingReceive(SmartHomeAgent.MESSAGE_TEMPLATE_SENDER_IS_COLLECTOR);
        logger.debug(Utils.parseAgentName(this.agent) + " received a message from " + Utils.parseAgentName(receivedMessage.getSender()) +
                "with contents: " + receivedMessage.getContent());
        try {
            this.agent.setcSum(Double.parseDouble(receivedMessage.getContent()));
        }catch(Exception e){
            logger.error("could not parse cSum sent from the data collector", e);
        }
        return messages;
    }

    protected void parseMessages(List<ACLMessage> messageList) {
        List<AgentIterationData> neighbors = new ArrayList<>();
        for (int i=0; i< messageList.size(); ++i)
        {
            try {
                neighbors.add((AgentIterationData)messageList.get(i).getContentObject());
            } catch (UnreadableException e) {
                logger.error("failed parsing the message contents with an exception", e);
            }
        }

        agent.setMyNeighborsShed(neighbors);
    }

    protected double calcPrice(double[] powerConsumption) {
        double res = 0 ;
        double [] priceScheme = agent.getAgentData().getPriceScheme();
        for (int i=0 ; i<priceScheme.length; ++i)
        {
            double temp =  powerConsumption[i] * priceScheme[i];
            res = Double.sum(temp, res);
        }
        return res;
    }

    // used by the agent instance to complete the initialization of the behaviour
    protected void initializeBehaviourWithAgent(SmartHomeAgent agent)
    {
        this.agent = agent;
        this.currentNumberOfIter =0;
        this.FINAL_TICK = agent.getAgentData().getBackgroundLoad().length -1;
        this.helper = new AlgorithmDataHelper(agent);
    }

    protected void addBackgroundLoadToPriceScheme(double[] powerConsumption)
    {
        double [] backgroundLoad = agent.getAgentData().getBackgroundLoad();
        for (int i=0 ; i<backgroundLoad.length; ++i)
        {
            powerConsumption[i] =  Double.sum(powerConsumption[i], backgroundLoad[i]);
        }
    }

    /**
     * agent might get messages from the AMS - agent management system, the smart home agent ignores these messages.
     * this method clears these messages from the agent's messages queue and prints their contents as warnings
     */
    private void clearAmsMessages()
    {
        ACLMessage receivedMessage;
        do
        {
            receivedMessage = this.agent.receive(SmartHomeAgent.MESSAGE_TEMPLATE_SENDER_IS_AMS);
            if (receivedMessage != null)
            {
                logger.warn(receivedMessage);
            }
        } while (receivedMessage != null);
    }


}
