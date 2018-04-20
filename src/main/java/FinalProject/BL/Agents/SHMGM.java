package FinalProject.BL.Agents;

import FinalProject.BL.IterationData.AgentIterationData;
import FinalProject.BL.IterationData.IterationCollectedData;
import FinalProject.Utils;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static FinalProject.BL.DataCollection.PowerConsumptionUtils.calculateEPeak;

public class SHMGM extends SmartHomeAgentBehaviour{

    private final static Logger logger = Logger.getLogger(SHMGM.class);
    private ImprovementMsg maxImprovementMsg = null; //used to calc msgs size only
    private final String gainMsgOntology = "GAIN_MSG";
    private MessageTemplate improvementTemplate = MessageTemplate.MatchOntology(gainMsgOntology);

    public SHMGM() {super();}

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
            receiveNeighboursIterDataAndHandleIt();
            improveSchedule(false);
        }
        this.currentNumberOfIter++;
    }

    private void receiveNeighboursIterDataAndHandleIt() {
        List<ACLMessage> messageList = waitForNeighbourMessages(SmartHomeAgent.MESSAGE_TEMPLATE_SENDER_IS_NEIGHBOUR);
        readNeighboursMsgs(messageList);
        List<double[]> neighboursSched = agent.getMyNeighborsShed().stream()
                .map(AgentIterationData::getPowerConsumptionPerTick)
                .collect(Collectors.toList());
        helper.calcPowerConsumptionForAllNeighbours(neighboursSched);
    }

    /**
     * Main logic of SH-MGM algo.
     * Calculate the best option for a schedule based on
     * neighbours schedule received, send the improvement to
     * all neighbours and receive theirs.
     * ONLY THE AGENT WITH THE GREATEST IMPROVEMENT SWITCHES TO THE NEW SCHEDULE!
     * Ties are solved using lexicographical ordering of agent's names.
     * @param randomPick
     */
    private void improveSchedule(boolean randomPick) {
        //backup prev iter's data
        AlgorithmDataHelper helperBackup = new AlgorithmDataHelper(helper);
        double[] prevIterPowerConsumption = helper.cloneArray(iterationPowerConsumption); //equals to agent.getCurrIteration().powerConsumptionPerTick
        AgentIterationData prevIterData = new AgentIterationData(agentIterationData);
        AgentIterationData prevCurrIterData = new AgentIterationData(agent.getCurrIteration());
        IterationCollectedData prevCollectedData = new IterationCollectedData(agentIterationCollected);
        double oldPrice = calcCsum(prevIterPowerConsumption);
        double prevTotalCost = helper.calcTotalPowerConsumption(oldPrice, iterationPowerConsumption); //also sets helper's epeak
        helper.totalPriceConsumption = prevTotalCost;
        System.out.println(agent.getLocalName() + " MY prev total cost is: " + prevTotalCost + ", actual_epeak: " + (prevTotalCost - oldPrice) + ", helper epeak: " + helper.ePeak);
        double prevAgentPriceSum = agent.getPriceSum();
        agent.setPriceSum(oldPrice);

        //calc try to improve sched
        helper.resetProperties();
        buildScheduleBasic(randomPick); //using Ci as priceSum

        //calculate improvement
        double newPrice = calcPrice(iterationPowerConsumption); //iterationPowerConsumption changed by buildScheduleBasic
        final double actualEpeak = tempBestPriceConsumption - newPrice;

        if(!randomPick) {
            double improvement = prevTotalCost - tempBestPriceConsumption;
            ImprovementMsg impMsg = sendImprovementToNeighbours(improvement, prevIterPowerConsumption);
            List<ImprovementMsg> receivedImprovements = receiveImprovements();
            receivedImprovements.add(impMsg);
            ImprovementMsg max = receivedImprovements.stream().max(ImprovementMsg::compareTo).orElse(null);
            maxImprovementMsg = max;

            if (max == null) {
                logger.error("max is null! Something went wrong!!!!!!!");
                resetToPrevIterationData(helperBackup, prevIterData, prevCollectedData, prevCurrIterData,
                        prevAgentPriceSum, prevIterPowerConsumption, null, null);
                return;
            }

            String maxName = Utils.cleanShtrudelFromAgentName(max.getAgentName());
            if (max.getImprovement() == 0.0) {
                logger.info(agent.getLocalName() + " 0 improvement, randomize schedule");
                improveSchedule(true);
            } else if (maxName.equals(agent.getLocalName())) { //take new schedule
                logger.info(agent.getLocalName() + "'s improvement: " + max.getImprovement() + " WAS THE GREATEST");
                takeNewSched(newPrice, actualEpeak);
            } else { //take prev schedule
                resetToPrevIterationData(helperBackup, prevIterData, prevCollectedData, prevCurrIterData,
                        prevAgentPriceSum, prevIterPowerConsumption, max.getImprevedSched(), max.getPrevSched());
            }
        }else{ //random pick of schedule
            logger.info(agent.getLocalName() + "is changing to random schedule");
            takeNewSched(newPrice, actualEpeak);
        }
    }

    private void takeNewSched(double newPrice, double actualEpeak) {
        agent.setPriceSum(newPrice);
        helper.totalPriceConsumption = tempBestPriceConsumption;
        helper.ePeak = actualEpeak;
        beforeIterationIsDone();
    }

    private void resetToPrevIterationData(AlgorithmDataHelper helperBackup, AgentIterationData prevIterData, IterationCollectedData prevCollectedData,
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

    private List<ImprovementMsg> receiveImprovements() {

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

    private ImprovementMsg sendImprovementToNeighbours(double improvement, double[] prevSched) {
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
    protected void generateScheduleForProp(PropertyWithData prop, double ticksToWork, Map<String, Integer> sensorsToCharge, boolean randomSched) {
        if (agent.isZEROIteration()) {
            startWorkZERO(prop, sensorsToCharge, ticksToWork);
        }
        else {
            startWorkNonZeroIter(prop, sensorsToCharge, ticksToWork, randomSched);
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

    @Override
    protected double calcImproveOptionGrade(double[] newPowerConsumption, List<double[]> allScheds) {
        double price = calcCsum(newPowerConsumption);
        return price + calculateEPeak(allScheds);

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SHMGM shmgm = (SHMGM) o;

        return super.equals(shmgm) && (maxImprovementMsg == null && shmgm.maxImprovementMsg == null) ||
                (maxImprovementMsg != null && shmgm.maxImprovementMsg != null &&
                        maxImprovementMsg.equals(shmgm.maxImprovementMsg));
    }

    @Override
    public int hashCode() {

        return Objects.hash(maxImprovementMsg, gainMsgOntology, improvementTemplate);
    }
}
