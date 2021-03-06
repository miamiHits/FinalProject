package FinalProject.BL.Agents;


import FinalProject.BL.DataCollection.DataCollectionCommunicator;
import FinalProject.BL.DataObjects.*;
import FinalProject.BL.Experiment;
import FinalProject.BL.IterationData.AgentIterationData;
import FinalProject.BL.IterationData.IterationCollectedData;
import FinalProject.Utils;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.OntologyException;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.domain.JADEAgentManagement.KillAgent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.wrapper.ControllerException;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static FinalProject.BL.DataCollection.PowerConsumptionUtils.calculateCSum;
import static FinalProject.BL.DataCollection.PowerConsumptionUtils.calculateTotalConsumptionWithPenalty;

public abstract class SmartHomeAgentBehaviour extends Behaviour implements Serializable{

    private final static Logger logger = Logger.getLogger(SmartHomeAgentBehaviour.class);
    private final String gainMsgOntology = "GAIN_MSG";
    public static final int START_TICK = 0;
    private final Random randGenerator = new Random();
    public SmartHomeAgent agent;
    protected int currentNumberOfIter;
    protected int FINAL_TICK;
    protected AlgorithmDataHelper helper;
    protected AgentIterationData agentIterationData;
    protected IterationCollectedData agentIterationCollected;
    protected ImprovementMsg maxImprovementMsg = null; //used to calc msgs size only

    protected boolean finished = false;
    protected double[] iterationPowerConsumption;
    protected double tempBestPriceConsumption = -1;
    protected MessageTemplate improvementTemplate;
    protected final int MSG_TO_DEVICE_SIZE = 4;
    protected Map<PropertyWithData, List<Set<Integer>>> propToSubsetsMap = new HashMap<>();
    protected Map<PropertyWithData, Map<String,Integer>> propToSensorsToChargeMap = new HashMap<>();

    public SmartHomeAgentBehaviour() {}

    public SmartHomeAgentBehaviour(SmartHomeAgent agent) {
        this.agent = agent;
    }

    //-------------ABSTRACT METHODS:-------------------

    /**
     * Main method implemented by inheriting algos!!
     * This method determines the algorithms' logic
     */
    protected abstract void doIteration();


    /**
     * Called by {@code done()} when the current pair
     * of (algo, problem) is done. Used for cleaning and logging.
     */
    protected abstract void onTermination();

    /**
     * generate schedule for the {@code prop} and update the sensors
     * @param prop the property to which the schedule should be generated
     * @param ticksToWork number of active ticks needed
     * @param sensorsToCharge sensors affected
     * @param randomSched flag to determine if the schedule for prop should be chosen at random (from legal ones)
     */
    protected abstract void generateScheduleForProp(PropertyWithData prop, double ticksToWork, Map<String, Integer> sensorsToCharge, boolean randomSched);

    /**
     * @return a deep copy of this {@link Behaviour}
     */
    public abstract SmartHomeAgentBehaviour cloneBehaviour();

    /**
     * Used to calculate the grade of a schedule option in order to decide
     * whether or not to choose it
     * @param newPowerConsumption the option for a schedule
     * @param allScheds a list of all schedules of neighbors and this agent
     * @return the grade of this option / schedule
     */
    protected abstract double calcImproveOptionGrade(double[] newPowerConsumption, List<double[]> allScheds);

    //-------------OVERRIDING METHODS:-------------------

    /**
     * JADE {@link Behaviour}'s main method.
     * The Template design pattern is used here to combine algorithm specific actions (logic)
     * together with actions such as sending data to {@link FinalProject.BL.DataCollection.DataCollector}.
     */
    @Override
    public void action() {
        doIteration();
        sendIterationToCollector();
        sendMsgToAllNeighbors(agent.getCurrIteration(), "");
        logger.debug("agent " + agent.getName() + " FINISHED ITER " + (currentNumberOfIter - 1));
    }

    /**
     * JADE {@link Behaviour}'s method.
     * @return true iff the run of this algorithm is done
     */
    @Override
    public boolean done() {
        boolean agentFinishedExperiment = currentNumberOfIter > Experiment.maximumIterations;
        if (agentFinishedExperiment) {

            //impl by child
            onTermination();

            this.agent.doDelete();

            try {
                agent.getContainerController().getAgent(agent.getLocalName()).kill();
                logger.debug(agent.getLocalName() + " called kill from container");
            } catch (ControllerException e) {
                logger.error("error killing agent " + agent.getLocalName() + " with container");
            }
//            try {
//                agent.getContentManager().registerLanguage(new SLCodec(), FIPANames.ContentLanguage.FIPA_SL);
//                agent.getContentManager().registerOntology(JADEManagementOntology.getInstance());
//
//                KillAgent killAgent = new KillAgent();
//                killAgent.setAgent(agent.getAID());
//                ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
//                msg.setOntology(JADEManagementOntology.NAME);
//                msg.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
//                msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
//                agent.getContentManager().fillContent(msg, new jade.content.onto.basic.Action(agent.getAMS(), killAgent));
//                msg.addReceiver(agent.getAMS());
//                agent.send(msg);
//                logger.debug(agent.getLocalName() + " send KILL msg to AMS");
//            } catch (Codec.CodecException | OntologyException e) {
//                logger.error("error killing agent " + agent.getLocalName() + " with AMS", e);
//            }
        }
        return agentFinishedExperiment;
    }


    //-------------PUBLIC METHODS:-------------------
    public AlgorithmDataHelper getHelper() {
        return helper;
    }

    public double[] getPowerConsumption() { return this.iterationPowerConsumption;}

    //-------------PROTECTED METHODS:-------------------

    /**
     * Init helper and build schedule
     */
    protected void buildScheduleFromScratch() {
        initHelper();
        buildScheduleBasic(false);
    }

    protected List<double[]> getNeighbourScheds() {
        return agent.getMyNeighborsShed().stream()
                .map(AgentIterationData::getPowerConsumptionPerTick)
                .collect(Collectors.toList());
    }

    /**
     * This is a <b>DEFAULT IMPLEMENTATION!</b> override it if it does not suit your needs.
     * Fill the fields agent.iterationMessageCount and agent.iterationMessageSize.
     * with the total size and number of messages send from an agent to it's
     * neighbours and to it's devices.
     */
    protected void countIterationCommunication() {
        int count = 1;

        //calc data sent to neighbours
        long totalSize = 0;
        long iterationDataSize = Utils.getSizeOfObj(agentIterationData);
        int neighboursSize = agent.getAgentData().getNeighbors().size();
        iterationDataSize *= neighboursSize;
        totalSize += iterationDataSize;
        count += neighboursSize;

        if (currentNumberOfIter > 0) {
            long improvementMsgSize = Utils.getSizeOfObj(maxImprovementMsg);
            improvementMsgSize *= neighboursSize;
            totalSize += improvementMsgSize;
            count += neighboursSize;
        }

        //calc messages to devices:
        final int constantNumOfMsgs = currentNumberOfIter == 0 ? 3 : 2;
        addMessagesSentToDevicesAndSetInAgent(count, totalSize, constantNumOfMsgs);
    }

    /**
     * Go back to the state the algorithm was before starting to calculate a schedule
     * for this iteration.
     * @param helperBackup helper's backup
     * @param prevIterData agentIterationData's backup
     * @param prevCollectedData agentIterationCollected's backup
     * @param prevCurrIterData agent.getCurrIteration()'s backup
     * @param prevPriceSum agent.getPriceSum()'s backup
     * @param prevIterPowerConsumption iterationPowerConsumption's backup
     * @param newBestSched the new best sched from all neighbours
     * @param prevBestSched the previous sched from the agent who's new sched is the most improved
     */
    protected void resetToPrevIterationData(AlgorithmDataHelper helperBackup, AgentIterationData prevIterData, IterationCollectedData prevCollectedData,
                                            AgentIterationData prevCurrIterData, double prevPriceSum,
                                            double[] prevIterPowerConsumption, double[] newBestSched, double[] prevBestSched) {
        helper = helperBackup;
        helper.correctEpeak(newBestSched, prevBestSched);

        agentIterationData = prevIterData;
        agentIterationData.setIterNum(currentNumberOfIter);

        agentIterationCollected = prevCollectedData;
        agentIterationCollected.setIterNum(currentNumberOfIter);
        agentIterationCollected.setePeak(-1); //sending epeak = -1 to collector if not improved

        agent.setCurrIteration(prevCurrIterData);
        agent.getCurrIteration().setIterNum(currentNumberOfIter);
        agent.setPriceSum(prevPriceSum);

        iterationPowerConsumption = prevIterPowerConsumption;
    }

    /**
     * Add the number and size of messages to devices and put in appropriate fields in agent.
     * @param count num of messages count to add to
     * @param totalSize total size of messages to add to
     * @param constantNumOfMsgs a constant num of messages to add for each device
     */
    protected void addMessagesSentToDevicesAndSetInAgent(int count, long totalSize, int constantNumOfMsgs) {
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
     * @param randomizeSched flag to determine if the schedule for prop
     *                       should be chosen at random (from legal ones)
     */
    protected void buildScheduleBasic(boolean randomizeSched) {
        tempBestPriceConsumption = helper.totalPriceConsumption;
        this.iterationPowerConsumption = new double[this.agent.getAgentData().getBackgroundLoad().length];
        addBackgroundLoadToPowerConsumption(iterationPowerConsumption);
        List<PropertyWithData> helperNonPassiveOnlyProps = helper.getAllProperties().stream()
                .filter(p -> !p.isPassiveOnly())
                .collect(Collectors.toList());
        for (PropertyWithData prop : helperNonPassiveOnlyProps) {
            double ticksToWork = helper.calcHowLongDeviceNeedToWork(prop);
            Map<String, Integer> sensorsToCharge = getSensorsToChargeForProp(prop, ticksToWork);
            generateScheduleForProp(prop, ticksToWork, sensorsToCharge, randomizeSched);
            if (prop.getDeltaWhenWorkOffline()<0) {
                makeSurePropBetweenRange(prop);
            }
            if (currentNumberOfIter > 0) {
                tempBestPriceConsumption = helper.calcTotalPowerConsumption(calcCsum(iterationPowerConsumption), iterationPowerConsumption);
            }

        }
    }

    protected void receiveNeighboursIterDataAndHandleIt() {
        List<ACLMessage> messageList = waitForNeighbourMessages(SmartHomeAgent.MESSAGE_TEMPLATE_SENDER_IS_NEIGHBOUR);
        readNeighboursMsgs(messageList);
        List<double[]> neighboursSched = getNeighbourScheds();
        helper.calcPowerConsumptionForAllNeighbours(neighboursSched);
    }


    /**
     * Calculate how many ticks a device needs to be charged in order to
     * work ticksToWork ticks.
     * @param key the name of the {@link PropertyWithData}
     * @param delta delta when charged
     * @param ticksToWork number of ticks the device needs to work
     * @return Number of ticks the device needs to be charged
     */
    protected int calcHowManyTicksNeedToCharge(String key, double delta, double ticksToWork) {
        int ticks = 0;
        PropertyWithData prop = helper.getAllProperties().stream()
                .filter(x -> x.getName().equals(key))
                .findFirst()
                .orElse(null);
        if (prop == null) {
            return -1;
        }

        double currState = prop.getSensor().getCurrentState();
        //lets see how many time we'll need to charge it.
        for (int i=0 ; i < ticksToWork; ++i) {
            if (currState < prop.getMin()) {
                currState += delta;
                ticks++;
            }
        }
        if (currState < prop.getMin()) {
            logger.warn("state is less than min!!!");
        }

        return ticks;
    }

    protected void initMsgTemplate() {
        MessageTemplate noAms = MessageTemplate.not(SmartHomeAgent.MESSAGE_TEMPLATE_SENDER_IS_AMS);
        improvementTemplate = MessageTemplate.and(MessageTemplate.MatchOntology(gainMsgOntology), noAms);
    }

    /**
     * Send the current iteration's data for this agent to the {@link FinalProject.BL.DataCollection.DataCollector}
     */
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

    /**
     * Wait for and receive {@link ImprovementMsg}s from all neighbours.
     * Used in algorithms such as {@link SHMGM}
     * @return a list of all the messages
     */
    protected List<ImprovementMsg> receiveImprovementMsgs() {

        List<ACLMessage> receivedMsgs = waitForNeighbourMessages(improvementTemplate);
        return receivedMsgs.stream()
                .map(msg -> {
                    try {
                        return (ImprovementMsg) msg.getContentObject();
                    } catch (UnreadableException e) {
                        logger.error("Could not read improvement msg: " + msg);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Send an {@link ImprovementMsg} to all the neighbours
     * to advertise how much this agent is able to improve the current schedule.
     * @param improvement improvement in total grade
     * @param prevSched schedule in previous iteration
     * @return the {@link ImprovementMsg} send.
     */
    protected ImprovementMsg sendImprovementToNeighbours(double improvement, double[] prevSched) {
        ImprovementMsg improvementToSend = new ImprovementMsg(agent.getName(), improvement,
                iterationPowerConsumption, prevSched);
        sendMsgToAllNeighbors(improvementToSend, gainMsgOntology);
        return improvementToSend;
    }

    /**
     * Send a message to all of this agent's neighbours.
     * @param msgContent the content of the message ({@link AgentIterationData} for example)
     * @param ontology the ontology used. This field is used to distinguish between message types
     *                 when reading them.
     */
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
            logger.error("failed sending message to neighbours with exception ", e);
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
        helper.setActuatorsAndSensors();
    }

    /**
     * @param start min value
     * @param last max value
     * @return a random int between start and last
     */
    protected int drawRandomNum(int start, int last) {
        return start + (int) (Math.random() * ((last - start) + 1));
    }

    /**
     * Work the device prop represents in ticks myTicks (update iterationPowerConsumption)
     * and charge sensors sensorsToCharge.
     * @param prop the property to work
     * @param myTicks the ticks to work in
     * @param sensorsToCharge map of <{@link Sensor}'s name, number of ticks to charge>
     */
    protected void updateTotals(PropertyWithData prop, List<Integer> myTicks, Map<String, Integer> sensorsToCharge) {
        List<Integer> activeTicks = helper.cloneList(myTicks);
        findActionToTicksMapAndPutTicks(prop, activeTicks);
        for (int i = 0; i < myTicks.size(); i++) {
            iterationPowerConsumption[myTicks.get(i)] += prop.getPowerConsumedInWork();
            if (sensorsToCharge != null && !sensorsToCharge.isEmpty()) {
                for (Map.Entry<String, Integer> entry : sensorsToCharge.entrySet()) {
                    PropertyWithData brother = helper.getAllProperties().stream()
                            .filter(property -> property.getName().equals(entry.getKey()))
                            .findFirst().orElse(null);
                    if (brother != null) {
                        brother.updateValueToSensor(this.iterationPowerConsumption, brother.getMin(), entry.getValue(), myTicks.get(i), true, activeTicks);
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

    /**
     * Create schedule for {@link PropertyWithData} prop <b>in iteration 0</b>.
     * @param prop the property to which the schedule should be generated
     * @param sensorsToCharge map of <{@link Sensor}'s name, number of ticks to charge>
     * @param ticksToWork number of ticks the device needs to work
     */
    protected void startWorkZERO(PropertyWithData prop, Map<String, Integer> sensorsToCharge, double ticksToWork) {
        if (ticksToWork <= 0) {
            List<Integer> activeTicks = helper.cloneList(prop.activeTicks);
            findActionToTicksMapAndPutTicks(prop, activeTicks);
            return;
        }
        List<Integer> myTicks = generateRandomTicksForProp(prop, ticksToWork);
        List<Integer> activeTicks = helper.cloneList(prop.activeTicks);
        findActionToTicksMapAndPutTicks(prop, activeTicks);

        updateTotals(prop, myTicks, sensorsToCharge);
    }

    /**
     * Create schedule for {@link PropertyWithData} prop <b>in iteration > 0</b>.
     * @param prop the property to which the schedule should be generated
     * @param sensorsToCharge map of <{@link Sensor}'s name, number of ticks to charge>
     * @param ticksToWork number of ticks the device needs to work
     * @param randomChoice flag to determine if the schedule for prop
     *                       should be chosen at random (from legal ones)
     */
    protected void startWorkNonZeroIter(PropertyWithData prop, Map<String, Integer> sensorsToCharge, double ticksToWork, boolean randomChoice) {
        prop.activeTicks.clear();

        List<Set<Integer>> subsets = getSubsetsForProp(prop, ticksToWork);
//        if (ticksToWork <= 0) {
//            subsets = checkAllSubsetOptions(prop);
//            if (subsets == null ) {
//                logger.warn("subsets is null!");
//                return;
//            }
//        }
//        else if (propToSubsetsMap.containsKey(prop)) {
//            subsets = propToSubsetsMap.get(prop);
//        }
//        else {
//            List<Integer> rangeForWork = calcRangeOfWork(prop);
//            subsets = helper.getSubsets(rangeForWork, (int) ticksToWork);
//        }

        if (!randomChoice) {
            lookForBestOptionAndApplyIt(prop, sensorsToCharge, subsets);
        }
        else{ //random choice
            applyRandomChoice(prop, sensorsToCharge, subsets);
        }
    }

    /**
     * @param probabilityForTrue the probability to return true
     * @return true with probability probabilityForTrue, else false
     */
    protected boolean flipCoin(float probabilityForTrue) {
        final boolean res = randGenerator.nextFloat()  < probabilityForTrue;
        return res;
    }

    /**
     * Calculate the ticks the device represented by prop can work in
     * @param prop the property to which the schedule should be generated
     * @return A list of tick indexes prop can work in.
     */
    protected List<Integer> calcRangeOfWork(PropertyWithData prop) {
        List<Integer> rangeForWork = new ArrayList<>();

        switch (prop.getPrefix())
        {
            case BEFORE: //NOT Include the hour
                for (int i = 0; i< prop.getTargetTick(); ++i) {
                    rangeForWork.add(i);
                }
                break;
            case AFTER:
                for (int i = 0; i <= prop.getTargetTick(); ++i) {
                    rangeForWork.add(i);
                }
                break;
            case AT:
                for (int i = 0; i < prop.getTargetTick(); ++i) {
                    rangeForWork.add(i);
                }
        }

        return rangeForWork;
    }

    /**
     * Calculate the best option for a schedule from the options in subsets
     * @param prop the property to which the best sched should be calculated
     * @param subsets options for ticks
     * @return a list of tick indexes for which the total grade is the lowest
     */
    protected List<Integer> calcBestPrice(PropertyWithData prop, List<Set<Integer>> subsets) {
        List<Integer> newTicks = new ArrayList<>();
        double [] newPowerConsumption = helper.cloneArray(agent.getCurrIteration().getPowerConsumptionPerTick());
        List<double[]> allScheds = getNeighbourScheds();
        int index = allScheds.size();
        List<Integer> prevTicks = getTicksForProp(prop);
        if (prevTicks == null) {
            logger.error("calcBestPrice: prevTicks is null!");
            return null;
        }
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
            newTicks = getTicksForProp(prop);
        }

        return newTicks;
    }

    /**
     * Calculate all possible sets of tick indexes for prop to work
     * @param prop the property to which the ticks are calculated
     * @return a {@link List} of {@link Set}s for all possible tick indexes
     */
    protected List<Set<Integer>> checkAllSubsetOptions(PropertyWithData prop) {
        if (propToSubsetsMap.containsKey(prop)) {
            return propToSubsetsMap.get(prop);
        }

        List<Integer> rangeForWork = calcRangeOfWork(prop);
        int numOfTicksInRange = rangeForWork.size();
        int ticksToWork = 0;
        double currState = prop.getSensor().getCurrentState();
        double targetValue = prop.getTargetValue();
        if (prop.getDeltaWhenWork() >= 0 && currState <= targetValue) {
            double deltaIfNoActiveWorkIsDone = (currState - targetValue) - ((Math.abs(prop.getDeltaWhenWorkOffline())) * numOfTicksInRange);
            //delta > 0 and deltaIfNoActiveWorkIsDone >= 0 => no work to be done
            if (deltaIfNoActiveWorkIsDone >= 0) {
                logger.warn("checkAllSubsetOptions: " + agent.getLocalName() + "'s deltaIfNoActiveWorkIsDone >= 0. returning empty subset!");
                return new ArrayList<>(0);
            }
            //delta > 0 and currState < 0 => need to work
            for (int i = 0; i < numOfTicksInRange; ++i) {
                ticksToWork++;
                deltaIfNoActiveWorkIsDone = deltaIfNoActiveWorkIsDone + prop.getDeltaWhenWork();
                if(deltaIfNoActiveWorkIsDone >= 0) {
                    break;
                }
            }
        }
        else if (prop.getDeltaWhenWork() < 0 && currState >= targetValue) {
            double deltaIfNoActiveWorkIsDone = (targetValue - currState) - ((Math.abs(prop.getDeltaWhenWorkOffline())) * numOfTicksInRange);
            //delta < 0 and deltaIfNoActiveWorkIsDone <= 0 => no work to be done
            if (deltaIfNoActiveWorkIsDone <= 0) {
                logger.warn("checkAllSubsetOptions: " + agent.getLocalName() + "'s deltaIfNoActiveWorkIsDone <= 0. returning empty subset!");
                return new ArrayList<>(0);
            }
            //delta < 0 and deltaIfNoActiveWorkIsDone > 0 => need to work {
            for (int i = 0; i < numOfTicksInRange; ++i) {
                ticksToWork++;
                deltaIfNoActiveWorkIsDone = deltaIfNoActiveWorkIsDone + prop.getDeltaWhenWork();
                if(deltaIfNoActiveWorkIsDone <= 0) {
                    break;
                }
            }
        }
        final List<Set<Integer>> subsets = helper.getSubsets(rangeForWork, ticksToWork);
        propToSubsetsMap.put(prop, subsets);
        return subsets;
    }

    /**
     * Get all subsets of tick indexes for {@link PropertyWithData} prop.
     * Calculates them if not already cached.
     * @param prop the property to which the ticks are calculated
     * @param ticksToWork number of ticks the device needs to work
     * @return a {@link List} of {@link Set}s for all possible tick indexes
     */
    protected List<Set<Integer>> getSubsetsForProp(PropertyWithData prop, double ticksToWork) {
        if (propToSubsetsMap.containsKey(prop)) {
            return propToSubsetsMap.get(prop);
        }

        List<Set<Integer>> subsets;
        if (ticksToWork <= 0) {
            subsets = checkAllSubsetOptions(prop);
            if (subsets == null ) {
                logger.warn("subsets is null!");
                subsets = null;
            }
        }
        else {
            List<Integer> rangeForWork = calcRangeOfWork(prop);
            subsets = helper.getSubsets(rangeForWork, (int) ticksToWork);
        }
        propToSubsetsMap.put(prop, subsets);
        return subsets;
    }

    /**
     * Calculate Csum for schedule sched.
     * @param sched schedule for this agent.
     * @return Csum for sched.
     */
    protected double calcCsum(double[] sched) {
        List<double[]> scheds = getNeighbourScheds();
        scheds.add(sched);
        return calculateCSum(scheds, agent.getAgentData().getPriceScheme());
    }

    /**
     * Fill data fields needed to send to {@link FinalProject.BL.DataCollection.DataCollector}
     * and neighbours and other fields.
     */
    protected void beforeIterationIsDone() {
        double price = calcPrice(this.iterationPowerConsumption);
        double[] arr = helper.cloneArray(this.iterationPowerConsumption);
        agentIterationData = new AgentIterationData(currentNumberOfIter, agent.getName(), price, arr);
        agent.setCurrIteration(agentIterationData);
        Set<String> neighboursNames = agent.getAgentData().getNeighbors().stream()
                .map(AgentData::getName)
                .collect(Collectors.toSet());
        countIterationCommunication();
        agentIterationCollected = new IterationCollectedData(currentNumberOfIter, agent.getName(),price, arr, agent.getProblemId(),
                agent.getAlgoId(), neighboursNames, helper.ePeak, agent.getIterationMessageSize(), agent.getIterationMessageCount());
        logger.debug("before done: " + agent.getLocalName() + " iter: " + currentNumberOfIter + " epeak: " + helper.ePeak + " price: " + price);
    }

    /**
     *a blocking method that waits far receiving messages from all neighbours,
     * and and clears all AMS messages
     * @return List of messages from all neighbours
     * @param msgTemplate the {@link MessageTemplate} for the messages to wait for
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

    /**
     * Parse the messages and put them in agent.myNeighboursSched.
     * @param messageList The messages to parse.
     */
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

    /**
     * Calculate the monetary cost of a schedule
     * @param powerConsumption power consumption per tick
     * @return the monetary cost for powerConsumption
     */
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
     * @param agent the agent to initialize
     */
    protected void initializeBehaviourWithAgent(SmartHomeAgent agent) {
        this.agent = agent;
        this.currentNumberOfIter = 0;
        this.FINAL_TICK = agent.getAgentData().getBackgroundLoad().length -1;
        this.helper = new AlgorithmDataHelper(agent);
    }

    /**
     * Add background load to an array of power consumption per tick
     * @param powerConsumption the array to add to
     */
    protected void addBackgroundLoadToPowerConsumption(double[] powerConsumption) {
        double [] backgroundLoad = agent.getAgentData().getBackgroundLoad();
        for (int i = 0 ; i < backgroundLoad.length; ++i) {
            powerConsumption[i] = powerConsumption[i] + backgroundLoad[i];
        }
    }

    /**
     * Get the ticks prop has worked in the previous iteration
     * @param prop the property to which the ticks belong
     * @return a list of tick indexes in which prop has worked in the previous iteration
     */
    protected List<Integer> getTicksForProp(PropertyWithData prop) {
        Action actionForProp = getActionForProp(prop);
        if (actionForProp == null) {
            logger.error("getTicksForProp: actionForProp is null!");
            return null;
        }
        Map<Action, List<Integer>> actionToTicks = helper.getDeviceToTicks().get(prop.getActuator());
        return actionToTicks.get(actionForProp);
    }

    //-------------PRIVATE METHODS:-------------------

    private void makeSurePropBetweenRange(PropertyWithData prop)
    {
        switch (prop.getPrefix())
        {
            case BEFORE: //NOT Include the hour
                prop.calcAndUpdateCurrState(FINAL_TICK, iterationPowerConsumption, false);
                break;
            case AFTER:
                prop.calcAndUpdateCurrState(START_TICK, iterationPowerConsumption, true);
                break;
            case AT:
                break;
        }
    }

    private Map<String, Integer> getSensorsToChargeForProp(PropertyWithData prop, double ticksToWork) {
        if (propToSensorsToChargeMap.containsKey(prop)) {
            return propToSensorsToChargeMap.get(prop);
        }
        Map<String, Integer> sensorsToCharge = new HashMap<>();
        //check if there is sensor in the same ACT who's delta is negative (has an offline effect, usually related to charge)
        prop.getRelatedSensorsDelta().forEach((sensorPropName, delta) -> {
            if (delta < 0) {
                int ticksNeedToCharge = calcHowManyTicksNeedToCharge(sensorPropName, delta, ticksToWork);
                if (ticksNeedToCharge > 0) {
                    sensorsToCharge.put(sensorPropName, ticksNeedToCharge);
                }
            }
        });
        propToSensorsToChargeMap.put(prop, sensorsToCharge);
        return sensorsToCharge;
    }

    private List<Integer> generateRandomTicksForProp(PropertyWithData prop, double ticksToWork) {
        if (ticksToWork > prop.getTargetTick()&& (prop.getPrefix()== Prefix.BEFORE || prop.getPrefix()== Prefix.AT)){
            ticksToWork = prop.getTargetTick();
        }

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
                    else {
                        double targetTick = prop.getTargetTick();
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
        Action actionForProp = getActionForProp(prop);
        if (actionForProp == null) {
            logger.error("updateAgentCurrIter: actionForProp is null!");
            return;
        }
        Map<Action, List<Integer>> actionToTicks = helper.getDeviceToTicks().get(prop.getActuator());
        List<Integer> prevTicks = actionToTicks.get(actionForProp);
        final double[] agentPowerConsumptionPerTick = agent.getCurrIteration().getPowerConsumptionPerTick();
        for (int i = 0; i < prevTicks.size(); i++) {
            agentPowerConsumptionPerTick[prevTicks.get(i)] -= prop.getPowerConsumedInWork(); //remove consumption from the prev tick
        }
        for (int i = 0; i < activeTicks.size(); i++) {
            agentPowerConsumptionPerTick[activeTicks.get(i)] += prop.getPowerConsumedInWork(); //add consumption to new tick
        }
    }

    private void lookForBestOptionAndApplyIt(PropertyWithData prop, Map<String, Integer> sensorsToCharge, List<Set<Integer>> subsets) {
        List<Integer> newTicks = calcBestPrice(prop, subsets);
        updateAgentCurrIter(prop, newTicks); //must be before update totals because uses helper.getDeviceToTicks().get(prop.getActuator())
        updateTotals(prop, newTicks, sensorsToCharge); //changes helper.getDeviceToTicks().get(prop.getActuator()) and iterationPowerConsumption
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

    private List<Integer> pickRandomScheduleForProp(PropertyWithData prop, List<Set<Integer>> subsets) {
        double [] newPowerConsumption = helper.cloneArray(agent.getCurrIteration().getPowerConsumptionPerTick());
        List<double[]> allScheds = getNeighbourScheds();
//        List<Integer> prevTicks = helper.getDeviceToTicks().get(prop.getActuator());
        Action actionForProp = getActionForProp(prop);
        if (actionForProp == null) {
            return null;
        }
        Map<Action, List<Integer>> actionToTicks = helper.getDeviceToTicks().get(prop.getActuator());
        List<Integer> prevTicks = actionToTicks.get(actionForProp);
        //remove current prop consumption
        for (Integer tick : prevTicks) {
            newPowerConsumption[tick] -= prop.getPowerConsumedInWork();
        }

        //pick random option
        Set<Integer> ticks = chooseRandomSubset(subsets);
        //Adding the ticks to the array
        for (Integer tick : ticks) {
            newPowerConsumption[tick] += prop.getPowerConsumedInWork();
        }
        allScheds.add(newPowerConsumption);
        double res = calcImproveOptionGrade(newPowerConsumption, allScheds);
        tempBestPriceConsumption = res;
        return new ArrayList<>(ticks);
    }

    private Action getActionForProp(PropertyWithData prop) {
        Action actionForProp = prop.getActuator().getActions().stream()
                .filter(action -> !action.getName().equals("off") &&
                        action.getPowerConsumption() == prop.getPowerConsumedInWork())
                .filter(action -> {
                    List<Effect> fx = action.getEffects();
                    return fx.stream()
                            .anyMatch(effect -> effect.getDelta() == prop.getDeltaWhenWork() &&
                                    effect.getProperty().equals(prop.getName()));
                })
                .findAny()
                .orElse(null);
        if (actionForProp == null) {
            logger.error("pickRandomScheduleForProp: no action for prop " + prop.getName() + " in it's actuator " + prop.getActuator().getName());
            return null;
        }

        return actionForProp;
    }

    private Set<Integer> chooseRandomSubset(List<Set<Integer>> subsets) {
        int size = subsets.size();
        if (size == 0 ){
            return new HashSet<Integer>();
        }
        int randomNum = ThreadLocalRandom.current().nextInt(0, size);
        return subsets.get(randomNum);
    }

    private void findActionToTicksMapAndPutTicks(PropertyWithData prop, List<Integer> activeTicks) {
        Map<Action, List<Integer>> actionToTicks = helper.getDeviceToTicks().get(prop.getActuator());
        if (actionToTicks == null) {
            actionToTicks = new HashMap<>();
            helper.getDeviceToTicks().put(prop.getActuator(), actionToTicks);
        }
        Action actionForProp = getActionForProp(prop);
        actionToTicks.put(actionForProp, activeTicks);
    }

    private void applyRandomChoice(PropertyWithData prop, Map<String, Integer> sensorsToCharge, List<Set<Integer>> subsets) {
        List<Integer> newTicks = pickRandomScheduleForProp(prop, subsets);
        updateAgentCurrIter(prop, newTicks); //must be before update totals because uses helper.getDeviceToTicks().get(prop.getActuator())
        updateTotals(prop, newTicks, sensorsToCharge); //changes helper.getDeviceToTicks().get(prop.getActuator()) and iterationPowerConsumption
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SmartHomeAgentBehaviour that = (SmartHomeAgentBehaviour) o;
        boolean agentEquals = agent.equals(that.agent);
        return currentNumberOfIter == that.currentNumberOfIter &&
                finished == that.finished &&
                that.tempBestPriceConsumption == tempBestPriceConsumption &&
                agentEquals &&
                helper.equals(that.helper) &&
                agentIterationData.equals(that.agentIterationData) &&
                agentIterationCollected.equals(that.agentIterationCollected) &&
                Arrays.equals(iterationPowerConsumption, that.iterationPowerConsumption);
    }
}
