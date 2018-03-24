package FinalProject.BL.Agents;
import FinalProject.Utils;
import jade.lang.acl.ACLMessage;
import org.apache.log4j.Logger;

import java.util.*;

import static FinalProject.BL.DataCollection.PowerConsumptionUtils.calculateTotalConsumptionWithPenalty;

public class DSA extends SmartHomeAgentBehaviour {

    private final static Logger logger = Logger.getLogger(DSA.class);

    public DSA()
    {
        super(); //invoke the Behaviour default constructor
    }

    public DSA(SmartHomeAgent agent) {
        super(agent);
        this.currentNumberOfIter =0;
        this.FINAL_TICK = agent.getAgentData().getBackgroundLoad().length;
        this.helper = new AlgorithmDataHelper(agent);

    }

    @Override
    public void doIteration() {
        if (agent.isZEROIteration()) {
            logger.info("Starting work on Iteration: 0");
            buildScheduleFromScratch();
            agent.setZEROIteration(false);
            logger.info("FINISH ITER 0");
        }
        else {
            receiveAllMessagesAndHandleThem();
            logger.info("Starting work on Iteration: " + this.currentNumberOfIter);
            resetAndBuildSchedule();
            logger.info("FINISHed ITER " + currentNumberOfIter);
        }
        beforeIterationIsDone();
        this.currentNumberOfIter++;
    }

    @Override
    public SmartHomeAgentBehaviour cloneBehaviour() {
        DSA newInstance = new DSA(); //TODO: maybe should pass an agent?
        newInstance.finished = this.finished;
        newInstance.currentNumberOfIter = this.currentNumberOfIter;
        newInstance.FINAL_TICK = this.FINAL_TICK;
        newInstance.agentIterationData = null; //will be created as part of the behaviour run(see buildScheduleFromScratch)
        return newInstance;
    }

    private void receiveAllMessagesAndHandleThem() {
        List<ACLMessage> messageList = waitForNeighbourMessages(SmartHomeAgent.MESSAGE_TEMPLATE_SENDER_IS_NEIGHBOUR);
        readNeighboursMsgs(messageList);
        updatePowerConsumption();
    }

    private void updatePowerConsumption() {
        helper.calcPowerConsumptionForAllNeighbours();
        agent.setPriceSum(calcCsum());
        helper.calcAndSetTotalPowerConsumption(agent.getPriceSum());
//        updateAgentIterationData(currentNumberOfIter - 1);
    }

    private void resetAndBuildSchedule() {
        helper.resetProperties();
        buildScheduleBasic();
    }

    @Override
    protected void generateScheduleForProp(PropertyWithData prop, double ticksToWork, Map<String, Double> sensorsToCharge) {
        float PROBABILITY = 0.6f;
        if (agent.isZEROIteration()) {
            startWorkZERO(prop, sensorsToCharge, ticksToWork);
        }
        else if (flipCoin(PROBABILITY)) {
            startWorkNonZeroIter(prop, sensorsToCharge, ticksToWork);
        }
        else {
            updateTotals(prop, prop.activeTicks, sensorsToCharge);
        }
    }

    @Override
    public void onTermination() {
        receiveAllMessagesAndHandleThem();
        logger.info(Utils.parseAgentName(this.agent) + " Just sent to DataCollector final calculations");
    }

    @Override
    protected void countIterationCommunication() {
        int count = 1;

        //calc data sent to neighbours
        long totalSize = Utils.getSizeOfObj(agentIterationData);
        int neighboursSize = agent.getAgentData().getNeighbors().size();
        count += neighboursSize;
        totalSize *= neighboursSize;

        //calc messages to devices:
        final int constantNumOfMsgs = currentNumberOfIter == 0 ? 3 : 2;

        addMessagesSentToDevicesAndSetInAgent(count, totalSize, constantNumOfMsgs);
    }
}
