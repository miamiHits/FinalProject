package FinalProject.BL.Agents;
import FinalProject.BL.Experiment;
import FinalProject.BL.IterationData.AgentIterationData;
import FinalProject.BL.IterationData.IterationCollectedData;
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

    @Override
    public DSA cloneBehaviour() {
        DSA newInstance = new DSA();
        newInstance.finished = this.finished;
        newInstance.currentNumberOfIter = this.currentNumberOfIter;
        newInstance.FINAL_TICK = this.FINAL_TICK;
        newInstance.agentIterationData = null;//will be created as part of the behaviour run(see buildScheduleFromScratch)
        return newInstance;
    }

    public double[] getPowerConsumption() { return this.iterationPowerConsumption;}

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
    //TODO move this up somehow!

    private void tryBuildScheduleBasic() {
        this.iterationPowerConsumption = new double[this.agent.getAgentData().getBackgroundLoad().length];
        List<PropertyWithData> helperNonPassiveOnlyProps = helper.getAllProperties().stream()
                .filter(p -> !p.isPassiveOnly())
                .collect(Collectors.toList());
        for(PropertyWithData prop : helperNonPassiveOnlyProps) {
            if (prop.getPrefix() == Prefix.BEFORE) {
                prop.calcAndUpdateCurrState(prop.getTargetValue(),START_TICK, this.iterationPowerConsumption, true);
            }
            //lets see what is the state of the curr & related sensors till then
            prop.calcAndUpdateCurrState(prop.getMin(),START_TICK, this.iterationPowerConsumption, true);
            double ticksToWork = helper.calcHowLongDeviceNeedToWork(prop);
            Map<String, Double> sensorsToCharge = new HashMap<>();
            //check if there is sensor in the same ACT that is negative (usually related to charge)
            for (Map.Entry<String,Double> entry : prop.getRelatedSensorsDelta().entrySet()) {
                if (entry.getValue() < 0) {
                    double rateOfCharge = calcHowOftenNeedToCharge(entry.getKey(),entry.getValue(), ticksToWork);
                    if (rateOfCharge > 0) {
                        sensorsToCharge.put(entry.getKey(), rateOfCharge);
                    }
                }
            }
            startWork(prop, ticksToWork, sensorsToCharge);
        }
    }

    private void startWork(PropertyWithData prop, double ticksToWork, Map<String, Double> sensorsToCharge) {
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

    private int calcHowOftenNeedToCharge(String key, double delta, double ticksToWork) {
        int tick = 0;
        PropertyWithData prop;
        prop = helper.getAllProperties().stream()
                .filter(x -> x.getName().equals(key))
                .findFirst()
                .orElse(null);
        if (prop == null) {
            logger.warn(agent.getAgentData().getName() + " Try to look for the related sensors, but not found like this");
            return -1;
        }

        double currState = prop.getSensor().getCurrentState();
        //lets see how many time we'll need to charge it.
        for (int i=0 ; i < ticksToWork; ++i) {
           currState += delta;
           if (currState < prop.getMin()) {
               tick++;
               currState = prop.getMax();
           }
        }

        //no need to charge it between the work. lets just update the sensor
        if (tick == 0) {
            Map<Sensor, Double> toSend = new HashMap<>();
            toSend.put(prop.getSensor(), currState);
            prop.getActuator().act(toSend);
        }

        return tick;
    }

    @Override
    public boolean done() {
        boolean agentFinishedExperiment = currentNumberOfIter > Experiment.maximumIterations;
        if (agentFinishedExperiment) {
            logger.info(Utils.parseAgentName(this.agent) + " ended its final iteration");
            logger.info(Utils.parseAgentName(this.agent) + " about to send data to DataCollector");

            receivedAllMessagesAndHandleThem();
            logger.info(Utils.parseAgentName(this.agent) + " Just sent to DataCollector final calculations");

            this.agent.doDelete();
        }
        return agentFinishedExperiment;
    }
}
