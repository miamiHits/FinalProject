package FinalProject.BL.Agents;
import FinalProject.BL.DataObjects.*;
import FinalProject.Utils;
import jade.lang.acl.ACLMessage;
import org.apache.log4j.Logger;
import java.util.*;
import java.util.stream.Collectors;

import static FinalProject.BL.DataCollection.PowerConsumptionUtils.calculateTotalConsumptionWithPenalty;

public class DSA extends SmartHomeAgentBehaviour {
    private final static Logger logger = Logger.getLogger(DSA.class);

    private final float PROBABILITY = 0.6f;

    public DSA()
    {
        super(); //invoke the Behaviour default constructor
    }

    public DSA(SmartHomeAgent agent) {
        this.agent = agent;
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
            receivedAllMessagesAndHandleThem();
            logger.info("Starting work on Iteration: " + this.currentNumberOfIter);
            tryBuildSchedule();
            logger.info("FINISHed ITER " + currentNumberOfIter);
        }
        beforeIterationIsDone();
        this.currentNumberOfIter++;
    }

    public AlgorithmDataHelper getHelper() {
        return helper;
    }

    public double[] getPowerConsumption() { return this.iterationPowerConsumption;}

    @Override
    public DSA cloneBehaviour() {
        DSA newInstance = new DSA();
        newInstance.finished = this.finished;
        newInstance.currentNumberOfIter = this.currentNumberOfIter;
        newInstance.FINAL_TICK = this.FINAL_TICK;
        newInstance.agentIterationData = null;//will be created as part of the behaviour run(see buildScheduleFromScratch)
        return newInstance;
    }

    private void receivedAllMessagesAndHandleThem() {
        List<ACLMessage> messageList = waitForNeighbourMessages();
        parseMessages(messageList);
        helper.calcPriceSchemeForAllNeighbours();
        helper.calcTotalPowerConsumption(agent.getcSum());
        sentEpeakToDataCollector(currentNumberOfIter-1);
    }

    private void tryBuildSchedule() {
        helper.goBackToStartValues();
        tryBuildScheduleBasic();
    }

    public void buildScheduleFromScratch() {
        initHelper();
        tryBuildScheduleBasic();
    }

    @Override
    protected void generateScheduleForProp(PropertyWithData prop, double ticksToWork, Map<String, Double> sensorsToCharge) {
        if (agent.isZEROIteration()) {
            startWorkZERO(prop, sensorsToCharge, ticksToWork);
        }
        else if (drawCoin(PROBABILITY)) {
          startWorkNonZeroIter(prop, sensorsToCharge, ticksToWork);
        }
        else {
            updateTotals(prop, prop.activeTicks, sensorsToCharge);
        }
    }

    //    @Override
//    public boolean done() {
//        boolean agentFinishedExperiment = currentNumberOfIter > Experiment.maximumIterations;
//        if (agentFinishedExperiment) {
//            logger.info(Utils.parseAgentName(this.agent) + " ended its final iteration");
//            logger.info(Utils.parseAgentName(this.agent) + " about to send data to DataCollector");
//
//
//            this.agent.doDelete();
//        }
//        return agentFinishedExperiment;
//    }

    @Override
    public void onTermination() {
        receivedAllMessagesAndHandleThem();
        logger.info(Utils.parseAgentName(this.agent) + " Just sent to DataCollector final calculations");
    }
}
