package FinalProject.BL.Agents;

import FinalProject.BL.DataObjects.Prefix;
import FinalProject.BL.IterationData.AgentIterationData;
import FinalProject.BL.IterationData.IterationCollectedData;
import FinalProject.Utils;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.util.leap.Comparable;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class SHMGM extends SmartHomeAgentBehaviour{

    private final static Logger logger = Logger.getLogger(SHMGM.class);
    private ImprovementMsg maxImprovementMsg = null; //used to calc msgs size only

    @Override
    protected void doIteration() {
        if (agent.isZEROIteration()) {
            logger.info("Starting work on Iteration: 0");
            buildScheduleFromScratch();
            agent.setZEROIteration(false);
            logger.info("FINISH ITER 0");
        }
        else {
            List<ACLMessage> messageList = waitForNeighbourMessages();
            readNeighboursMsgs(messageList);
            improveSchedule();
        }
        beforeIterationIsDone();
        this.currentNumberOfIter++;
    }

    private void improveSchedule() {
        //backup prev values
        //TODO: test backup well!
        double[] prevIterPowerConsumption = helper.cloneArray(iterationPowerConsumption);
        AgentIterationData prevIterData = new AgentIterationData(agentIterationData);
        AgentIterationData prevCurrIterData = new AgentIterationData(agent.getCurrIteration());
        IterationCollectedData prevCollectedData = new IterationCollectedData(agentIterationCollected);
        double prevCsum = calcCsum();
        helper.calcTotalPowerConsumption(prevCsum);
        double prevTotalCost = helper.totalPriceConsumption;

        helper.resetProperties();
        buildScheduleBasic();

        //calculate improvement
        double newCsum = calcCsum();
        helper.calcTotalPowerConsumption(newCsum);
        double newTotalCost = helper.totalPriceConsumption;
        double improvement = newTotalCost - prevTotalCost;

        List<ImprovementMsg> receivedImprovements = sendAndReceiveImprovement(improvement);
        ImprovementMsg max = receivedImprovements.stream().max(ImprovementMsg::compareTo).orElse(null);
        maxImprovementMsg = max;
        if (max == null) {
            logger.error("max is null! Something went wrong!!!!!!!");
            resetToPrevIterationData(prevIterData, prevCollectedData, prevCurrIterData, prevCsum, prevTotalCost, prevIterPowerConsumption);
        }
        else if (max.agentName.equals(agent.getName())) { //take new schedule
            agent.setcSum(prevCsum);
        }
        else {
            resetToPrevIterationData(prevIterData, prevCollectedData, prevCurrIterData, prevCsum, prevTotalCost, prevIterPowerConsumption);
        }

    }

    //TODO: test this well!
    private void resetToPrevIterationData(AgentIterationData prevIterData, IterationCollectedData prevCollectedData,
                                          AgentIterationData prevCurrIterData, double prevCsum,
                                          double prevTotalCost, double[] prevIterPowerConsumption) {
        this.agentIterationData = prevIterData;
        this.agentIterationCollected = prevCollectedData;
        this.agent.setCurrIteration(prevCurrIterData);
        agent.setcSum(prevCsum);
        helper.totalPriceConsumption = prevTotalCost;
        this.iterationPowerConsumption = prevIterPowerConsumption;
    }

    private List<ImprovementMsg> sendAndReceiveImprovement(double improvement) {
        ImprovementMsg toSend = new ImprovementMsg(agent.getName(), improvement, agent.getIterationNum());
        sendMsgToAllNeighbors(toSend);
        List<ACLMessage> receivedMesgs = waitForNeighbourMessages();
        List<ImprovementMsg> improvements = receivedMesgs.stream()
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
        return improvements;
    }

    @Override
    protected void onTermination() {
        logger.info(agent.getName() + " for problem " + agent.getProblemId() + "and algo SH-MGM is TERMINATING!");
    }

    @Override
    protected void countIterationCommunication() {
        int count = 2; //2 for agentIterationCollected and agentIterationData

        //calc data sent to neighbours
        long totalSize = 0;
        long iterationDataSize = Utils.getSizeOfObj(agentIterationData);
        int neighboursSize = agent.getAgentData().getNeighbors().size();
        iterationDataSize *= neighboursSize;
        if (currentNumberOfIter > 0) {
            long improvementMsgSize = Utils.getSizeOfObj(maxImprovementMsg);
            improvementMsgSize *= neighboursSize;
            totalSize += improvementMsgSize;
            count += neighboursSize;
        }
        count += neighboursSize;
        totalSize += iterationDataSize;

        //calc data sent to DC:
        totalSize += Utils.getSizeOfObj(agentIterationCollected);

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
    public DSA cloneBehaviour() {
        DSA newInstance = new DSA();
        newInstance.finished = this.finished;
        newInstance.currentNumberOfIter = this.currentNumberOfIter;
        newInstance.FINAL_TICK = this.FINAL_TICK;
        newInstance.agentIterationData = null; //will be created as part of the behaviour run(see buildScheduleFromScratch)
        return newInstance;
    }

    private class ImprovementMsg implements Serializable, Comparable{
        private String agentName;
        private double improvement;
        private int iterNum;

        public ImprovementMsg(String agentName, double improvement, int iterNum) {
            this.agentName = agentName;
            this.improvement = improvement;
            this.iterNum = iterNum;
        }

        public String getAgentName() {
            return agentName;
        }

        public void setAgentName(String agentName) {
            this.agentName = agentName;
        }

        public double getImprovement() {
            return improvement;
        }

        public void setImprovement(double improvement) {
            this.improvement = improvement;
        }

        public int getIterNum() {
            return iterNum;
        }

        public void setIterNum(int iterNum) {
            this.iterNum = iterNum;
        }

        @Override
        public int compareTo(Object other) {
            if (other instanceof ImprovementMsg) {
                ImprovementMsg otherCast = (ImprovementMsg) other;
                double compare = this.improvement - otherCast.improvement;
                if (compare == 0) {
                    return this.agentName.compareTo(otherCast.agentName);
                }
                return compare > 0 ? 1 : -1;
            }
            logger.warn("ImprovementMsg.compareTo called to compare with non-ImprovementMsg, returning 0");
            return 0;
        }
    }
}
