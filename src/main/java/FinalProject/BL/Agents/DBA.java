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

public class DBA extends SmartHomeAgentBehaviour{

    private final static Logger logger = Logger.getLogger(DBA.class);
    private ImprovementMsg maxImprovementMsg = null; //used to calc msgs size only
    private final String gainMsgOntology = "GAIN_MSG";
    private MessageTemplate improvementTemplate;
    /* will tell about the "bags" the agent will have on specific ticks
       according to DBA logic
    */
    private int[] ticksBag = new int [iterationPowerConsumption.length];
    private int bag = 1;

    public DBA() { super(); }


    @Override
    protected void doIteration() {
        if (agent.isZEROIteration()) {
            buildScheduleFromScratch();
            initMsgTemplate(); // needs to be here to make sure SmartHomeAgent class is init
            agent.setZEROIteration(false);
            agent.setPriceSum(calcCsum(iterationPowerConsumption));
            beforeIterationIsDone();
        }
        else {
            receiveNeighboursIterDataAndHandleIt();
            improveSchedule(false);
        }
        this.currentNumberOfIter++;
    }


    /**
     * Main logic of DBA algo.
     * Calculate the best option for a schedule based on
     * neighbours schedule received, send the improvement to
     * all neighbours and receive theirs.
     * If ALL the agents got 0 in their improvement , and in
     * 0.6 probabilty , the agent will add "bag" that will start in 1
     * to specific ticks. Will be done per agent.
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
        double prevAgentPriceSum = agent.getPriceSum();
        agent.setPriceSum(oldPrice);

        //calc try to improve sched
        helper.resetProperties();
        buildScheduleBasic(randomPick);

        int x = 2;
        //calculate improvement
        double newPrice = calcCsum(iterationPowerConsumption); //iterationPowerConsumption changed by buildScheduleBasic
        final double actualEpeak = tempBestPriceConsumption - newPrice;

        if(!randomPick) {
            double improvement = prevTotalCost - tempBestPriceConsumption;
            int y = 2;

            ImprovementMsg impMsg = sendImprovementToNeighbours(improvement, prevIterPowerConsumption);
            List<ImprovementMsg> receivedImprovements = receiveImprovementMsgs();
            receivedImprovements.add(impMsg);
            ImprovementMsg max = receivedImprovements.stream().max(ImprovementMsg::compareTo).orElse(null);
            maxImprovementMsg = max;

            if (max == null) {
                logger.error("max is null! Something went wrong!!!!!!!");
                resetToPrevIterationData(helperBackup, prevIterData, prevCollectedData, prevCurrIterData,
                        prevAgentPriceSum, prevIterPowerConsumption, null, null);
                return;
            }

            String maxName = Utils.parseAgentName(max.getAgentName());
            if (max.getImprovement() == 0.0) {
                improveSchedule(true);
            } else if (maxName.equals(agent.getLocalName())) { //take new schedule
                takeNewSched(newPrice, actualEpeak);
            } else { //take prev schedule
                resetToPrevIterationData(helperBackup, prevIterData, prevCollectedData, prevCurrIterData,
                        prevAgentPriceSum, prevIterPowerConsumption, max.getImprevedSched(), max.getPrevSched());
            }
        }else{ //random pick of schedule
            takeNewSched(newPrice, actualEpeak);
        }
    }

    private void takeNewSched(double newPrice, double actualEpeak) {
        agent.setPriceSum(newPrice);
        helper.totalPriceConsumption = tempBestPriceConsumption;
        helper.ePeak = actualEpeak;
        beforeIterationIsDone();
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
    public DBA cloneBehaviour() {
        DBA newInstance = new DBA();
        newInstance.finished = this.finished;
        newInstance.currentNumberOfIter = this.currentNumberOfIter;
        newInstance.FINAL_TICK = this.FINAL_TICK;
        newInstance.agentIterationData = null;
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
        DBA shmgm = (DBA) o;

        boolean superEquals = super.equals(shmgm);
        return superEquals && (maxImprovementMsg == null && shmgm.maxImprovementMsg == null) ||
                (maxImprovementMsg != null && shmgm.maxImprovementMsg != null &&
                        maxImprovementMsg.equals(shmgm.maxImprovementMsg));
    }

    @Override
    public int hashCode() {
        return Objects.hash(maxImprovementMsg, gainMsgOntology, improvementTemplate);
    }
}
