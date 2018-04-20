package FinalProject.BL.Agents;


import FinalProject.BL.DataCollection.DataCollectionCommunicator;
import FinalProject.BL.DataObjects.AgentData;
import FinalProject.BL.DataObjects.Prefix;
import FinalProject.BL.DataObjects.Rule;
import FinalProject.BL.DataObjects.Sensor;
import FinalProject.BL.Experiment;
import FinalProject.BL.IterationData.AgentIterationData;
import FinalProject.BL.IterationData.IterationCollectedData;
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
import java.util.stream.Collectors;

import static FinalProject.BL.DataCollection.PowerConsumptionUtils.calculateCSum;
import static FinalProject.BL.DataCollection.PowerConsumptionUtils.calculateTotalConsumptionWithPenalty;

public abstract class SmartHomeAgentBehaviour extends Behaviour implements Serializable{

    public static final int START_TICK = 0;
    private final Random randGenerator = new Random();
    public SmartHomeAgent agent;
    protected int currentNumberOfIter;
    protected int FINAL_TICK;
    protected AlgorithmDataHelper helper;
    protected AgentIterationData agentIterationData;
    protected IterationCollectedData agentIterationCollected;

    private final static Logger logger = Logger.getLogger(SmartHomeAgentBehaviour.class);
    protected boolean finished = false;
    protected double[] iterationPowerConsumption;
    protected double tempBestPriceConsumption = -1;

    public SmartHomeAgentBehaviour() {}

    public SmartHomeAgentBehaviour(SmartHomeAgent agent) {
        this.agent = agent;
    }

    //-------------ABSTRACT METHODS:-------------------

    /**
     * Main method implemented by inheriting algos!!!
     */
    protected abstract void doIteration();

    /**
     * Called by {@code done()} when returning true
     */
    protected abstract void onTermination();

    /**
     * @return the total size of messages send from an agent to it's
     * neighbours + total size of messages send to it's devices.
     */
    protected abstract void countIterationCommunication();

    /**
     * generate schedule for the {@code prop} and update the sensors
     * @param prop the property to which the schedule should be generated
     * @param ticksToWork number of active ticks needed
     * @param sensorsToCharge sensors affected
     */
    protected abstract void generateScheduleForProp(PropertyWithData prop, double ticksToWork, Map<String, Integer> sensorsToCharge);

    /**
     *
     * @return a deep copy of this {@link Behaviour}
     */
    public abstract SmartHomeAgentBehaviour cloneBehaviour();

    /**
     * Used by calcBestPrice method to calculate the grade of a schedule option in order to decide
     * whether or not to choose it
     * @param newPowerConsumption the option for a schedule
     * @param allScheds a list of all schedules of neighbors and this agent
     * @return the grade of this option / schedule
     */
    protected abstract double calcImproveOptionGrade(double[] newPowerConsumption, List<double[]> allScheds);

    //-------------OVERRIDING METHODS:-------------------
    @Override
    public void action() {
        doIteration();
        sendIterationToCollector();
        sendMsgToAllNeighbors(agent.getCurrIteration(), "");
        logger.info("agent + " + agent.getName() + " FINISHED ITER " + (currentNumberOfIter - 1));
    }

    @Override
    public boolean done() {
        boolean agentFinishedExperiment = currentNumberOfIter > Experiment.maximumIterations;
        if (agentFinishedExperiment) {

            //impl by child
            onTermination();

            this.agent.doDelete();
        }
        return agentFinishedExperiment;
    }

    //-------------PROTECTED METHODS:-------------------

    public AlgorithmDataHelper getHelper() {
        return helper;
    }

    public double[] getPowerConsumption() { return this.iterationPowerConsumption;}

    public void buildScheduleFromScratch() {
        initHelper();
        buildScheduleBasic(false);
    }

    protected void addMessagesSentToDevicesAndSetInAgent(int count, long totalSize, int constantNumOfMsgs) {
        final int MSG_TO_DEVICE_SIZE = 4;
        for (PropertyWithData prop : helper.getAllProperties()) {
            int numOfTimes = constantNumOfMsgs + prop.getRelatedSensorsDelta().size();
            if (prop.getPrefix() != null && prop.getPrefix().equals(Prefix.BEFORE)) {
                numOfTimes++;
            }
            totalSize += numOfTimes * MSG_TO_DEVICE_SIZE;
            count += numOfTimes;
        }

        agent.setIterationMessageCount(count);
        agent.setIterationMessageSize(totalSize);
    }

    /**
     * Go through all properties and generate schedule for them
     * @param randomizeSched
     */
    protected void buildScheduleBasic(boolean randomizeSched) {
        tempBestPriceConsumption = helper.totalPriceConsumption;
        this.iterationPowerConsumption = new double[this.agent.getAgentData().getBackgroundLoad().length];
        addBackgroundLoadToPowerConsumption(iterationPowerConsumption);
        List<PropertyWithData> helperNonPassiveOnlyProps = helper.getAllProperties().stream()
                .filter(p -> !p.isPassiveOnly())
                .collect(Collectors.toList());
        for (PropertyWithData prop : helperNonPassiveOnlyProps) {
            if (prop.getPrefix() == Prefix.BEFORE) {
                prop.calcAndUpdateCurrState(prop.getTargetValue(),START_TICK, this.iterationPowerConsumption, true);
            }
            //lets see what is the state of the curr & related sensors till then
            prop.calcAndUpdateCurrState(prop.getMin(),START_TICK, this.iterationPowerConsumption, true);
            double ticksToWork = helper.calcHowLongDeviceNeedToWork(prop);
            Map<String, Integer> sensorsToCharge = new HashMap<>();
            //check if there is sensor in the same ACT who's delta is negative (has an offline effect, usually related to charge)
            prop.getRelatedSensorsDelta().forEach((key, value) -> {
                if (value < 0) {
                    int ticksNeedToCharge = calcHowManyTicksNeedToCharge(key, value, ticksToWork);
                    if (ticksNeedToCharge > 0) {
                        sensorsToCharge.put(key, ticksNeedToCharge);
                    }
                }
            });
            if (!randomizeSched){
                generateScheduleForProp(prop, ticksToWork, sensorsToCharge);
            }
            else{ //random pick

            }
            if (currentNumberOfIter > 0) {
                tempBestPriceConsumption = helper.calcTotalPowerConsumption(calcPrice(iterationPowerConsumption), iterationPowerConsumption);
            }
        }
    }

    protected int calcHowManyTicksNeedToCharge(String key, double delta, double ticksToWork) {
        int ticks = 0;
        PropertyWithData prop;
        prop = helper.getAllProperties().stream()
                .filter(x -> x.getName().equals(key))
                .findFirst()
                .orElse(null);
        if (prop == null) {
//            logger.warn(agent.getAgentData().getName() + " Try to look for the related sensors, but not found like this");
            return -1;
        }

        double currState = prop.getSensor().getCurrentState();
        //lets see how many time we'll need to charge it.
        for (int i=0 ; i < ticksToWork; ++i) {
            if (currState < prop.getMin()) {
                currState += delta;
                ticks++;
//                currState = prop.getMax();
            }
        }
        if (currState < prop.getMin()) {
            logger.warn("state is less than min!!!");
        }

        return ticks;
    }

    protected void sendIterationToCollector() {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(DataCollectionCommunicator.SERVICE_TYPE);

        template.addServices(sd);

        try {
            //find data collector
            DFAgentDescription[] result = DFService.search(this.agent, template);
            if (result.length > 0) {

                //send the msg
                ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
                message.setOntology(agent.getProblemId() + agent.getAlgoId());
                for (DFAgentDescription foundAID : result) {
                    message.addReceiver(foundAID.getName());
                }
                message.setContentObject(agentIterationCollected);
                logger.debug(String.format("sending iteration #%d data to data collector", agentIterationCollected.getIterNum()));
                logger.debug(String.format("%s's sent epeak to collector is: " + agentIterationCollected.getePeak(), agentIterationCollected.getAgentName()));

                agent.send(message);
            }
            else {
                logger.error("could not find the data communicator");
            }
        }
        catch (FIPAException | IOException | NullPointerException e) {
            logger.error(e);
        }

    }

    protected void sendMsgToAllNeighbors(Serializable msgContent, String ontology) {
        ACLMessage aclMsg = new ACLMessage(ACLMessage.REQUEST);
        aclMsg.setOntology(ontology);
        agent.getAgentData().getNeighbors().stream()
                .map(neighbor -> new AID(neighbor.getName(), AID.ISLOCALNAME))
                .forEach(aclMsg::addReceiver);

        try {
            aclMsg.setContentObject(msgContent);
            agent.send(aclMsg);
        } catch (IOException e) {
//            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    protected void initHelper() {
        //classifying the rules by activeness, start creating the prop object
        List<Rule> passiveRules = agent.getAgentData().getRules().stream()
                .filter(rule -> !rule.isActive())
                .collect(Collectors.toList());
        List <Rule> activeRules = agent.getAgentData().getRules().stream()
                .filter(Rule::isActive)
                .collect(Collectors.toList());

        passiveRules.forEach(pRule -> helper.buildNewPropertyData(pRule, true));
        activeRules.forEach(rRule -> helper.buildNewPropertyData(rRule, false));
        helper.checkForPassiveRules();
        helper.SetActuatorsAndSensors();
    }

    protected int drawRandomNum(int start, int last) {
        return start + (int) (Math.random() * ((last - start) + 1));
    }

    protected void updateTotals(PropertyWithData prop, List<Integer> myTicks, Map<String, Integer> sensorsToCharge) {
        List<Integer> activeTicks = helper.cloneList(myTicks);
        helper.getDeviceToTicks().put(prop.getActuator(), activeTicks);
        for (int i = 0; i < myTicks.size(); ++i) {
            iterationPowerConsumption[myTicks.get(i)] += prop.getPowerConsumedInWork();
            if (!sensorsToCharge.isEmpty()) {
                for (Map.Entry<String,Integer> entry : sensorsToCharge.entrySet()) {
                    PropertyWithData brother = helper.getAllProperties().stream()
                            .filter(property -> property.getName().equals(entry.getKey()))
                            .findFirst().orElse(null);
                    int timeToCharge = (i + 1) % entry.getValue();
                    if (i == timeToCharge && brother != null) {
                        brother.updateValueToSensor(this.iterationPowerConsumption, brother.getMin(), entry.getValue(), i, true);
                    }
                }
            }
        }

        //update the sensor
        double currState = prop.getSensor().getCurrentState() + (prop.getDeltaWhenWork() * myTicks.size());
        if (currState > prop.getMax()) {
            currState = prop.getMax();
        }
        Map<Sensor, Double> sensorToStateMap = new HashMap<>();
        sensorToStateMap.put(prop.getSensor(), currState);
        prop.getActuator().act(sensorToStateMap);
    }

    protected void startWorkZERO(PropertyWithData prop, Map<String, Integer> sensorsToCharge, double ticksToWork) {
        if (ticksToWork <= 0) {
            prop.calcAndUpdateCurrState(prop.getTargetValue(), FINAL_TICK, iterationPowerConsumption, false);
            List<Integer> activeTicks = helper.cloneList(prop.activeTicks);
            helper.getDeviceToTicks().put(prop.getActuator(), activeTicks);
            return;
        }
        List<Integer> myTicks = generateRandomTicksForProp(prop, ticksToWork);
        List<Integer> activeTicks = helper.cloneList(prop.activeTicks);
        helper.getDeviceToTicks().put(prop.getActuator(), activeTicks);
        updateTotals(prop, myTicks, sensorsToCharge);
    }

    protected void startWorkNonZeroIter(PropertyWithData prop, Map<String, Integer> sensorsToCharge, double ticksToWork, boolean randomChoice) {
        prop.activeTicks.clear();

        List<Set<Integer>> subsets;
        if (ticksToWork <= 0) {
            System.out.println(agent.getLocalName() + " ticks to work is " + ticksToWork);
            subsets = checkAllSubsetOptions(prop);
            if (subsets == null) {
                logger.error("subsets is null!");
                return;
            }
        }
        else {
            List<Integer> rangeForWork = calcRangeOfWork(prop);
            subsets = helper.getSubsets(rangeForWork, (int) ticksToWork);
        }
        if (!randomChoice) {
            lookForBestOptionAndApplyIt(prop, sensorsToCharge, subsets);
        }
        else{ //random choice
            applyRandomChoice(prop, sensorsToCharge, subsets);
        }
    }

    private void applyRandomChoice(PropertyWithData prop, Map<String, Integer> sensorsToCharge, List<Set<Integer>> subsets) {
        List<Integer> newTicks = pickRandomSchedule(prop, subsets);
        updateAgentCurrIter(prop, newTicks); //must be before update totals because uses helper.getDeviceToTicks().get(prop.getActuator())
        updateTotals(prop, newTicks, sensorsToCharge); //changes helper.getDeviceToTicks().get(prop.getActuator()) and iterationPowerConsumption
    }

    private List<Integer> pickRandomSchedule(PropertyWithData prop, List<Set<Integer>> subsets) {
        List<Integer> newTicks = new ArrayList<>();
        double [] newPowerConsumption = helper.cloneArray(agent.getCurrIteration().getPowerConsumptionPerTick());
        List<double[]> allScheds = agent.getMyNeighborsShed().stream()
                .map(AgentIterationData::getPowerConsumptionPerTick)
                .collect(Collectors.toList());
        int index = allScheds.size();
        List<Integer> prevTicks = helper.getDeviceToTicks().get(prop.getActuator());
        //remove them from the array
        for (Integer tick : prevTicks) {
            newPowerConsumption[tick] -= prop.getPowerConsumedInWork();
        }
        double[] copyOfNew = helper.cloneArray(newPowerConsumption);

        boolean improved = false;
        //find the best option
        for(Set<Integer> ticks : subsets) {
            //Adding the ticks to array
            for (Integer tick : ticks) {
                newPowerConsumption[tick] += prop.getPowerConsumedInWork();
            }
            allScheds.add(newPowerConsumption);
            double res = calcImproveOptionGrade(newPowerConsumption, allScheds);

            if (res <= helper.totalPriceConsumption && res <= tempBestPriceConsumption) {
                tempBestPriceConsumption = res;
                newTicks.clear();
                newTicks.addAll(ticks);
                improved = true;
            }

            //reset
            newPowerConsumption = helper.cloneArray(copyOfNew);
            allScheds.remove(index); //remove this new sched
        }

        if(!improved) {
            newTicks = helper.getDeviceToTicks().get(prop.getActuator());
        }

        return newTicks;
    }

    private void lookForBestOptionAndApplyIt(PropertyWithData prop, Map<String, Integer> sensorsToCharge, List<Set<Integer>> subsets) {
        List<Integer> newTicks = calcBestPrice(prop, subsets);
        updateAgentCurrIter(prop, newTicks); //must be before update totals because uses helper.getDeviceToTicks().get(prop.getActuator())
        updateTotals(prop, newTicks, sensorsToCharge); //changes helper.getDeviceToTicks().get(prop.getActuator()) and iterationPowerConsumption
    }

    protected boolean flipCoin(float probabilityForTrue) {
        final boolean res = randGenerator.nextFloat() < probabilityForTrue;
        System.out.println("res is: " + res);
        return res;
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

    protected List<Integer> calcBestPrice(PropertyWithData prop, List<Set<Integer>> subsets) {
        List<Integer> newTicks = new ArrayList<>();
        double [] newPowerConsumption = helper.cloneArray(agent.getCurrIteration().getPowerConsumptionPerTick());
        List<double[]> allScheds = agent.getMyNeighborsShed().stream()
                .map(AgentIterationData::getPowerConsumptionPerTick)
                .collect(Collectors.toList());
        int index = allScheds.size();
        List<Integer> prevTicks = helper.getDeviceToTicks().get(prop.getActuator());
        //remove them from the array
        for (Integer tick : prevTicks) {
            newPowerConsumption[tick] -= prop.getPowerConsumedInWork();
        }
        double[] copyOfNew = helper.cloneArray(newPowerConsumption);

        boolean improved = false;
        //find the best option
        for(Set<Integer> ticks : subsets) {
            //Adding the ticks to array
            for (Integer tick : ticks) {
                newPowerConsumption[tick] += prop.getPowerConsumedInWork();
            }
            allScheds.add(newPowerConsumption);
            double res = calcImproveOptionGrade(newPowerConsumption, allScheds);

            if (res <= helper.totalPriceConsumption && res <= tempBestPriceConsumption) {
                tempBestPriceConsumption = res;
                newTicks.clear();
                newTicks.addAll(ticks);
                improved = true;
            }

            //reset
            newPowerConsumption = helper.cloneArray(copyOfNew);
            allScheds.remove(index); //remove this new sched
        }

        if(!improved) {
            newTicks = helper.getDeviceToTicks().get(prop.getActuator());
        }

        return newTicks;
    }

    protected List<Set<Integer>> checkAllSubsetOptions(PropertyWithData prop) {
        List<Integer> rangeForWork =  calcRangeOfWork(prop);
         double currState = prop.getSensor().getCurrentState();
         double minVal = prop.getTargetValue();
         double deltaIfNoActiveWorkIsDone = (currState - minVal) - ((Math.abs(prop.getDeltaWhenWorkOffline())) * rangeForWork.size());
         int ticksToWork = 0;
         if (deltaIfNoActiveWorkIsDone > 0) {
             return null;
         }
         for (int i = 0; i < rangeForWork.size(); ++i) {
             ticksToWork++;
             deltaIfNoActiveWorkIsDone = Double.sum(deltaIfNoActiveWorkIsDone, prop.getDeltaWhenWork());
             if(deltaIfNoActiveWorkIsDone > 0) {
                 break;
             }
         }
        return helper.getSubsets(rangeForWork, ticksToWork);
    }

    protected double calcCsum(double[] sched) {
        List<double[]> scheds = agent.getMyNeighborsShed().stream()
                .map(AgentIterationData::getPowerConsumptionPerTick)
                .collect(Collectors.toList());
        scheds.add(sched);
        return calculateCSum(scheds, agent.getAgentData().getPriceScheme());
    }

    protected void beforeIterationIsDone() {
        //addBackgroundLoadToPowerConsumption(this.iterationPowerConsumption);
        double price = calcPrice(this.iterationPowerConsumption);
        double[] arr = helper.cloneArray(this.iterationPowerConsumption);
        logger.info("my PowerCons is: " + arr[0] + "," +  arr[1] + "," + arr[2] +"," + arr[3] + "," + arr[4] +"," + arr[5] + "," +arr[6] );
        logger.info("my PRICE is: " + price);
        logger.info("my EPEAK is: " + helper.ePeak);
        logger.info("my ITER is: " + currentNumberOfIter);
        agentIterationData = new AgentIterationData(currentNumberOfIter, agent.getName(), price, arr);
        agent.setCurrIteration(agentIterationData);
        Set<String> neighboursNames = agent.getAgentData().getNeighbors().stream()
                .map(AgentData::getName)
                .collect(Collectors.toSet());
        countIterationCommunication();
        agentIterationCollected = new IterationCollectedData(currentNumberOfIter, agent.getName(),price, arr, agent.getProblemId(),
                agent.getAlgoId(), neighboursNames, helper.ePeak, agent.getIterationMessageSize(), agent.getIterationMessageCount());
    }


    /**
     *a blocking method that waits far receiving messages from all neighbours,
     * and and clears all AMS messages
     * @return List of messages from all neighbours
     * @param msgTemplate
     */
    protected List<ACLMessage> waitForNeighbourMessages(MessageTemplate msgTemplate) {
        List<ACLMessage> messages = new ArrayList<>();
        ACLMessage receivedMessage;
        int neighbourCount = this.agent.getAgentData().getNeighbors().size();
        while (messages.size() < neighbourCount) {
            receivedMessage = this.agent.blockingReceive(msgTemplate);
            messages.add(receivedMessage);
        }

        //clear queue from ams messages
        clearAmsMessages();

        return messages;
    }

    protected void readNeighboursMsgs(List<ACLMessage> messageList) {
        List<AgentIterationData> neighbours = new ArrayList<>();
        messageList.forEach(msg -> {
            try {
                neighbours.add((AgentIterationData) msg.getContentObject());
            } catch (UnreadableException e) {
                logger.error("failed parsing the message contents with an exception", e);
            }
        });

        agent.setMyNeighborsShed(neighbours);
    }

    protected double calcPrice(double[] powerConsumption) {
        double res = 0 ;
        double [] priceScheme = agent.getAgentData().getPriceScheme();

        for (int i = 0 ; i < priceScheme.length; ++i) {
            res += powerConsumption[i] * priceScheme[i];
        }
        return res;
    }

    /**
     * used by the agent instance to complete the initialization of the behaviour
     * @param agent
     */
    protected void initializeBehaviourWithAgent(SmartHomeAgent agent) {
        this.agent = agent;
        this.currentNumberOfIter = 0;
        this.FINAL_TICK = agent.getAgentData().getBackgroundLoad().length -1;
        this.helper = new AlgorithmDataHelper(agent);
    }

    protected void addBackgroundLoadToPowerConsumption(double[] powerConsumption) {
        double [] backgroundLoad = agent.getAgentData().getBackgroundLoad();
        for (int i = 0 ; i < backgroundLoad.length; ++i) {
            powerConsumption[i] = powerConsumption[i] + backgroundLoad[i];
        }
    }

    //-------------PRIVATE METHODS:-------------------

    private List<Integer> generateRandomTicksForProp(PropertyWithData prop, double ticksToWork) {
        List<Integer> myTicks = new ArrayList<>();
        //generate random schedule based on prop's rules
        int randomNum = 0;
        for (int i = 0; i < ticksToWork; ++i) {
            switch (prop.getPrefix()) {
                case BEFORE:    // Min + (int)(Math.random() * ((Max - Min) + 1)). NOT INCLUDE THE HOUR
                    randomNum = START_TICK + (int) (Math.random() * (((prop.getTargetTick() - 1) - START_TICK) + 1));
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
                    else {   double targetTick = prop.getTargetTick();
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
            else if (!myTicks.contains(randomNum)) {
                myTicks.add(randomNum);
            }
            else {
                i--;
            }
        }
        return myTicks;
    }
    private void updateAgentCurrIter(PropertyWithData prop, List<Integer> newTicks) {
        List<Integer> activeTicks = helper.cloneList(newTicks);
        List<Integer> prevTicks = helper.getDeviceToTicks().get(prop.getActuator());
        final double[] agentPowerConsumptionPerTick = agent.getCurrIteration().getPowerConsumptionPerTick();
        for (int i = 0; i < prevTicks.size(); i++) {
            agentPowerConsumptionPerTick[prevTicks.get(i)] -= prop.getPowerConsumedInWork(); //remove consumption from the prev tick
        }
        for (int i = 0; i < activeTicks.size(); i++) {
            agentPowerConsumptionPerTick[activeTicks.get(i)] += prop.getPowerConsumedInWork(); //add consumption to new tick
        }
    }

    /**
     * agent might get messages from the AMS - agent management system, the smart home agent ignores these messages.
     * this method clears these messages from the agent's messages queue and prints their contents as warnings
     */
    private void clearAmsMessages() {
        ACLMessage receivedMessage;
        do
        {
            receivedMessage = this.agent.receive(SmartHomeAgent.MESSAGE_TEMPLATE_SENDER_IS_AMS);
            if (receivedMessage != null) {
                logger.warn(receivedMessage);
            }
        } while (receivedMessage != null);
    }

    protected void pickRandomSched() {
        helper.resetProperties();
        buildScheduleBasic(true); //build random schedule
    }


}
