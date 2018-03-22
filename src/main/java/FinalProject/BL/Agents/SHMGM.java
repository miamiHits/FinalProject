package FinalProject.BL.Agents;

import FinalProject.BL.IterationData.AgentIterationData;
import FinalProject.BL.IterationData.IterationCollectedData;
import FinalProject.Utils;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.util.leap.Comparable;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
        }
        else {
            logger.info("Starting work on Iteration: " + currentNumberOfIter);
            List<ACLMessage> messageList = waitForNeighbourMessages();
            readNeighboursMsgs(messageList);
            helper.calcPowerConsumptionForAllNeighbours(); //TODO added
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
        double oldPrice = calcPrice(prevIterPowerConsumption);
        double prevTotalCost = helper.calcTotalPowerConsumption(oldPrice); //also sets helper's epeak
        double prevAgentPriceSum = agent.getPriceSum();
        agent.setPriceSum(oldPrice);

        helper.resetProperties();
        buildScheduleBasic(); //using Ci as priceSum

        //calculate improvement
        double newPrice = calcPrice(iterationPowerConsumption); //iterationPowerConsumption changed by buildScheduleBasic
        double newTotalCost = helper.calcTotalPowerConsumption(newPrice);
        double improvement = newTotalCost - prevTotalCost;

        List<ImprovementMsg> receivedImprovements = sendAndReceiveImprovement(improvement);
        ImprovementMsg max = receivedImprovements.stream().max(ImprovementMsg::compareTo).orElse(null);
        maxImprovementMsg = max;
        if (max == null) {
            logger.error("max is null! Something went wrong!!!!!!!");
            //TODO: maybe use oldPrice instead of prevAgentPriceSum
            resetToPrevIterationData(prevIterData, prevCollectedData, prevCurrIterData, prevAgentPriceSum, prevTotalCost, prevIterPowerConsumption);
        }
        else if (max.getAgentName().equals(agent.getName())) { //take new schedule
            logger.info(agent.getName() + "'s improvement: " + max.getImprovement() + " WAS THE GREATEST");
            agent.setPriceSum(newPrice);
        }
        else { //take prev schedule
            logger.info(agent.getName() + " got max improvement: " + max.getImprovement() + " from agent " + max.getAgentName());
            //TODO: maybe use oldPrice instead of prevAgentPriceSum
            resetToPrevIterationData(prevIterData, prevCollectedData, prevCurrIterData, prevAgentPriceSum, prevTotalCost, prevIterPowerConsumption);
        }

    }

    //TODO: test this well!
    private void resetToPrevIterationData(AgentIterationData prevIterData, IterationCollectedData prevCollectedData,
                                          AgentIterationData prevCurrIterData, double prevPriceSum,
                                          double prevTotalCost, double[] prevIterPowerConsumption) {
        this.agentIterationData = prevIterData;
        this.agentIterationCollected = prevCollectedData;
        this.agent.setCurrIteration(prevCurrIterData);
        agent.setPriceSum(prevPriceSum);
        helper.totalPriceConsumption = prevTotalCost;
        this.iterationPowerConsumption = prevIterPowerConsumption;
    }

    private List<ImprovementMsg> sendAndReceiveImprovement(double improvement) {
        logger.info(agent.getName() + " sending improvement to neighbours");
        ImprovementMsg improvementToSend = new ImprovementMsg(agent.getName(), improvement, agent.getIterationNum());
        sendMsgToAllNeighbors(improvementToSend);
        List<ACLMessage> receivedMsgs = waitForNeighbourMessages();
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
