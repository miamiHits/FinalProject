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
    private MessageTemplate improvementTemplate;

    public SHMGM() { super(); }

    @Override
    protected void doIteration() {
        if (agent.isZEROIteration()) {
            initMsgTemplate(); // needs to be here to make sure SmartHomeAgent class is init
            buildScheduleFromScratch();
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
        double prevAgentPriceSum = agent.getPriceSum();
        agent.setPriceSum(oldPrice);

        //calc try to improve sched
        helper.resetProperties();
        buildScheduleBasic(randomPick);

        //calculate improvement
        double newPrice = calcCsum(iterationPowerConsumption); //iterationPowerConsumption changed by buildScheduleBasic
        final double actualEpeak = tempBestPriceConsumption - newPrice;

        if(!randomPick) {
            double improvement = prevTotalCost - tempBestPriceConsumption;
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
            if (max.getImprovement() <= 0.0) {
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
    protected void generateScheduleForProp(PropertyWithData prop, double ticksToWork, Map<String, Integer> sensorsToCharge, boolean randomSched) {
        if (agent.isZEROIteration()) {
            startWorkZERO(prop, sensorsToCharge, ticksToWork);
        }
        else {
            startWorkNonZeroIter(prop, sensorsToCharge, ticksToWork, randomSched);
        }
    }

    @Override
    public SHMGM cloneBehaviour() {
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
