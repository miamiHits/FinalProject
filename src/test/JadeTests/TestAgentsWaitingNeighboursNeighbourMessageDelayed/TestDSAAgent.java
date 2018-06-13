package JadeTests.TestAgentsWaitingNeighboursNeighbourMessageDelayed;

import FinalProject.BL.Agents.DSA;
import FinalProject.BL.Agents.SmartHomeAgent;
import FinalProject.BL.Agents.SmartHomeAgentBehaviour;
import FinalProject.BL.DataObjects.AgentData;
import FinalProject.BL.IterationData.IterationCollectedData;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.stream.Collectors;

public class TestDSAAgent extends DSA{

    int delayIterationInterval; //agent shall delay the sending of a message to its neighbours every @delayIterationInterval iteration
    TestAgentsWaitingNeighboursNeighbourMessageDelayed testDriver;
    private int currentIterationNumber = 0;

    private final static Logger logger = Logger.getLogger(TestDSAAgent.class);

    public TestDSAAgent(TestAgentsWaitingNeighboursNeighbourMessageDelayed testDriver,
                        int delayIterationInterval)
    {
        this.testDriver = testDriver;
        this.delayIterationInterval = delayIterationInterval;
    }

    @Override
    public void action()
    {
        if (currentNumberOfIter > testDriver.MAXIMUM_ITERATIONS)
        {
            logger.info("completed test");
            return;
        }

        if (this.currentIterationNumber > 0)
        {
            waitForNeighbourMessages(SmartHomeAgent.MESSAGE_TEMPLATE_SENDER_IS_NEIGHBOUR);
        }

        logger.info("started iteration #" + this.currentIterationNumber);
        this.agentIterationCollected = new IterationCollectedData(
                currentIterationNumber,
                agent.getAgentData().getName(),
                10,
                new double[12],
                testDriver.problem.getId(),
                testDriver.algorithm.getBehaviourName(),
                (agent.getAgentData().getNeighbors().stream().map(AgentData::getName).collect(Collectors.toSet())),
                12, 1, 1);

        sendIterationToCollector();
        sendMsgToAllNeighbors(this.agentIterationCollected, "");
        currentIterationNumber++;
    }

    @Override
    public boolean done()
    {
        if (currentIterationNumber > testDriver.MAXIMUM_ITERATIONS)
        {
            testDriver.notifyTestPassed("success");
            return true;
        }
        else
        {
            logger.debug("not done yet, current iteration is " + currentIterationNumber);
        }
        return false;
    }

    @Override
    public SmartHomeAgentBehaviour cloneBehaviour() {
        logger.debug("current behaviour being cloned");
        TestDSAAgent newInstance = new TestDSAAgent(this.testDriver, this.delayIterationInterval);
        newInstance.finished = this.finished;
        newInstance.currentNumberOfIter = this.currentNumberOfIter;
        newInstance.FINAL_TICK = this.FINAL_TICK;
        newInstance.agentIterationData = null; //will be created as part of the behaviour run(see buildScheduleFromScratch)
        return newInstance;

    }

    @Override
    protected void sendMsgToAllNeighbors(Serializable msgContent, String ontology) {
        if (this.currentIterationNumber % this.delayIterationInterval == 0)
        {
            try {
                logger.info("starting a sleep delay");
                Thread.sleep(TestAgentsWaitingNeighboursNeighbourMessageDelayed.MESSAGE_DELAY_IN_MILLISEC);
                logger.debug("completed a sleep delay");
            } catch (InterruptedException e) {
                String message = this.agent.getAgentData().getName() + " had an exception while sleeping to delay message sending";
                logger.error(message, e);
                testDriver.notifyTestFailed(message);
            }
        }
        logger.info("sending message to neighbours");
        super.sendMsgToAllNeighbors(msgContent, ontology);
    }
}
