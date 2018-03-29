package FinalProject.BL.Agents;

import FinalProject.BL.IterationData.AgentIterationData;
import FinalProject.BL.IterationData.IterationCollectedData;
import FinalProject.Utils;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class SHMGM extends SmartHomeAgentBehaviour{

    private final static Logger logger = Logger.getLogger(SHMGM.class);
    private ImprovementMsg maxImprovementMsg = null; //used to calc msgs size only
    private final String gainMsgOntology = "GAIN_MSG";
    private MessageTemplate improvementTemplate = MessageTemplate.MatchOntology(gainMsgOntology);

    public SHMGM() {super();}

    public SHMGM(SmartHomeAgent agent) {
        super(agent);
        this.agent = agent;
        helper = new AlgorithmDataHelper(agent);
    }

    @Override
    protected void doIteration() {
        if (agent.isZEROIteration()) {
            logger.info("Starting work on Iteration: 0");
            buildScheduleFromScratch();
            agent.setZEROIteration(false);
            agent.setPriceSum(calcPrice(iterationPowerConsumption));
            beforeIterationIsDone();
        }
        else {
            logger.info("Starting work on Iteration: " + currentNumberOfIter);
            List<ACLMessage> messageList = waitForNeighbourMessages(SmartHomeAgent.MESSAGE_TEMPLATE_SENDER_IS_NEIGHBOUR);
            readNeighboursMsgs(messageList);
            List<double[]> neighboursSched = agent.getMyNeighborsShed().stream()
                    .map(AgentIterationData::getPowerConsumptionPerTick)
                    .collect(Collectors.toList());
//            neighboursSched.add(iterationPowerConsumption);
            helper.calcPowerConsumptionForAllNeighbours(neighboursSched);
            improveSchedule();
        }
//        beforeIterationIsDone(); //TODO check if its good
        this.currentNumberOfIter++;
    }

    private void improveSchedule() {
        //backup prev values
        //TODO: test backup well!

        System.out.println(agent.getLocalName() + "'s iter " + currentNumberOfIter + " sched BEFORE is: " + Arrays.toString(iterationPowerConsumption) + " $$$ " + Arrays.toString(agent.getCurrIteration().getPowerConsumptionPerTick()));

        //TODO: jump in inter 8, exactly equals diff between new actual epeak and helper epeak - impro
        AlgorithmDataHelper helperBackup = new AlgorithmDataHelper(helper);
        double[] prevIterPowerConsumption = helper.cloneArray(iterationPowerConsumption); //equals to agent.getCurrIteration().powerConsumptionPerTick
        AgentIterationData prevIterData = new AgentIterationData(agentIterationData);
        AgentIterationData prevCurrIterData = new AgentIterationData(agent.getCurrIteration());
        IterationCollectedData prevCollectedData = new IterationCollectedData(agentIterationCollected);
        double oldPrice = calcPrice(prevIterPowerConsumption);
        double prevTotalCost = helper.calcTotalPowerConsumption(oldPrice, iterationPowerConsumption); //also sets helper's epeak
        helper.totalPriceConsumption = prevTotalCost;
        System.out.println(agent.getLocalName() + " MY prev total cost is: " + prevTotalCost + ", actual_epeak: " + (prevTotalCost - oldPrice) + ", helper epeak: " + helper.ePeak);

        double oldEpeak = helper.ePeak;
        double prevAgentPriceSum = agent.getPriceSum();
        agent.setPriceSum(oldPrice);

        helper.resetProperties();
        buildScheduleBasic(); //using Ci as priceSum

        //calculate improvement
        double newPrice = calcPrice(iterationPowerConsumption); //iterationPowerConsumption changed by buildScheduleBasic
        double newTotalCost = tempBestPriceConsumption; //TODO: delete this var
        double newTotalCostHelper = helper.calcTotalPowerConsumption(newPrice, iterationPowerConsumption);
//        double newTotalCost = helper.calcTotalPowerConsumption(newPrice, iterationPowerConsumption);

        double improvement =  prevTotalCost - newTotalCost;
        final double actualEpeak = newTotalCost - newPrice;
        System.out.println(agent.getLocalName() + "'s iter " + currentNumberOfIter + " prevTotalCost: " + prevTotalCost +
                ", newTotalCost: " + newTotalCost + ", oldPrice: " + oldPrice +", newPrice: " +
                newPrice + ", impro: " + improvement + ", new actual epeak: " + actualEpeak);

        ImprovementMsg impMsg = sendImprovementToNeighbours(improvement, prevIterPowerConsumption);
        List<ImprovementMsg> receivedImprovements = receiveImprovements();
        receivedImprovements.add(impMsg);
        ImprovementMsg max = receivedImprovements.stream().max(ImprovementMsg::compareTo).orElse(null);
        maxImprovementMsg = max;
        if (max == null) {
            logger.error("max is null! Something went wrong!!!!!!!");
            //TODO: maybe use oldPrice instead of prevAgentPriceSum
            resetToPrevIterationData(helperBackup, prevIterData, prevCollectedData, prevCurrIterData,
                    prevAgentPriceSum, prevIterPowerConsumption, null, null);
            return;
        }

        String agentName = beautifyAgentName(agent.getName());
        String maxName = beautifyAgentName(max.getAgentName());
        logger.info("agent is: " + agentName +" max is: " + maxName + " iter num: " + this.currentNumberOfIter);

        if (maxName.equals(agentName)) { //take new schedule
            logger.info(agent.getName() + "'s improvement: " + max.getImprovement() + " WAS THE GREATEST");
            agent.setPriceSum(newPrice);
//            helper.totalPriceConsumption = newTotalCost;
//            List<double[]> neighboursSched = agent.getMyNeighborsShed().stream().map(AgentIterationData::getPowerConsumptionPerTick).collect(Collectors.toList());
//            neighboursSched.add(iterationPowerConsumption);
//            helper.calcPowerConsumptionForAllNeighbours(neighboursSched);
//            double bestTotalCons = helper.calcTotalPowerConsumption(newPrice, iterationPowerConsumption);
            helper.totalPriceConsumption = tempBestPriceConsumption;
            helper.ePeak = actualEpeak;
            System.out.println(agent.getLocalName() + "is BEST! " + currentNumberOfIter + " prevTotalCost: " + prevTotalCost +
                    ", newTotalCost: " + newTotalCost + ", oldPrice: " + oldPrice +", newPrice: " +
                    newPrice + ", impro: " + improvement + " bestTotalCost: " + tempBestPriceConsumption + ", new actual epeak: " +
                    actualEpeak + ", helper epeak: " + helper.ePeak + ", newTotalCostHelper: " + newTotalCostHelper + ", diff: " + (newTotalCostHelper - tempBestPriceConsumption));
            beforeIterationIsDone();
        }
        else { //take prev schedule
            logger.info(agent.getName() + " got max improvement: " + max.getImprovement() + " from agent " + max.getAgentName());
            resetToPrevIterationData(helperBackup, prevIterData, prevCollectedData, prevCurrIterData,
                    prevAgentPriceSum, prevIterPowerConsumption, max.getImprevedSched(), max.getPrevSched());
            agentIterationCollected.setIterNum(currentNumberOfIter);
//            double epeakAfterImpro = max.getEpeakDiff(agent.getAgentData().getPriceScheme());
//            helper.ePeak = epeakAfterImpro;
            agentIterationCollected.setePeak(-1); //sending epeak = -1 to collector if not improved
        }

        System.out.println(agent.getLocalName() + "'s iter " + currentNumberOfIter + " sched AFTER is: " + Arrays.toString(iterationPowerConsumption) + " $$$ " + Arrays.toString(agent.getCurrIteration().getPowerConsumptionPerTick()));


    }

    private String beautifyAgentName(String name) {
        int shtrudel = name.indexOf('@');
        if (shtrudel != -1){
            name = name.substring(0, shtrudel);
        }
        return name;
    }

    //TODO: rename params
    private void resetToPrevIterationData(AlgorithmDataHelper helperBackup, AgentIterationData prevIterData, IterationCollectedData prevCollectedData,
                                          AgentIterationData prevCurrIterData, double prevPriceSum,
                                          double[] prevIterPowerConsumption, double[] newBestSched, double[] prevBestSched) {
        helper = helperBackup;
        helper.correctEpeak(newBestSched, prevBestSched);
        agentIterationData = prevIterData;
        agentIterationCollected = prevCollectedData;
        agentIterationCollected.setIterNum(currentNumberOfIter);
        agentIterationCollected.setePeak(helper.ePeak);
        agent.setCurrIteration(prevCurrIterData);
        agent.getCurrIteration().setIterNum(currentNumberOfIter);
        agent.setPriceSum(prevPriceSum);
        agentIterationData.setIterNum(currentNumberOfIter);
        iterationPowerConsumption = prevIterPowerConsumption;

    }

    private List<ImprovementMsg> receiveImprovements() {

        List<ACLMessage> receivedMsgs = waitForNeighbourMessages(improvementTemplate);
        List<ImprovementMsg> improvements = receivedMsgs.stream()
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
        logger.info(agent.getName() + " got improvement to neighbours");
        return improvements;
    }

    private ImprovementMsg sendImprovementToNeighbours(double improvement, double[] prevSched) {
        logger.info(agent.getLocalName() + " sending improvement to neighbours");
        ImprovementMsg improvementToSend = new ImprovementMsg(agent.getName(), improvement, agent.getIterationNum(),
                iterationPowerConsumption, prevSched);
        sendMsgToAllNeighbors(improvementToSend, gainMsgOntology);
        return improvementToSend;
    }

    @Override
    protected void onTermination() {
        logger.info(agent.getName() + " for problem " + agent.getProblemId() + "and algo SH-MGM is TERMINATING!");
    }

    @Override
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

    @Override
    protected void generateScheduleForProp(PropertyWithData prop, double ticksToWork, Map<String, Double> sensorsToCharge) {
        if (agent.isZEROIteration()) {
            startWorkZERO(prop, sensorsToCharge, ticksToWork);
        }
        else {
            startWorkNonZeroIter(prop, sensorsToCharge, ticksToWork);
        }
    }

    @Override
    public SmartHomeAgentBehaviour cloneBehaviour() {
        SHMGM newInstance = new SHMGM();
        newInstance.finished = this.finished;
        newInstance.currentNumberOfIter = this.currentNumberOfIter;
        newInstance.FINAL_TICK = this.FINAL_TICK;
        newInstance.agentIterationData = null; //will be created as part of the behaviour run(see buildScheduleFromScratch)
        return newInstance;
    }

}
