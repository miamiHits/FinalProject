package FinalProject.BL.Agents;

import jade.lang.acl.ACLMessage;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;

public class SHMGM extends SmartHomeAgentBehaviour{

    private final static Logger logger = Logger.getLogger(SHMGM.class);

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
            helper.calcTotalPowerConsumption(agent.getcSum());
        }
        beforeIterationIsDone();
        this.currentNumberOfIter++;
    }

    @Override
    protected void onTermination() {

    }

    @Override
    protected void countIterationCommunication() {

    }

    @Override
    protected void generateScheduleForProp(PropertyWithData prop, double ticksToWork, Map<String, Double> sensorsToCharge) {

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
}
