package FinalProjectTests.Experiment.TestAgentsWaitingNeighboursNeighbourMessageDelayed;

import FinalProject.BL.DataCollection.DataCollectionCommunicatorBehaviour;
import FinalProject.BL.DataObjects.AgentData;
import FinalProject.BL.IterationData.IterationCollectedData;
import FinalProjectTests.Experiment.AbstractJadeIntegrationTest;
import jade.domain.DFService;
import jade.lang.acl.ACLMessage;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class TestDataCollectionCommunicatorBehaviour extends DataCollectionCommunicatorBehaviour {

    private int currentIterationNumber = 0;
    TestAgentsWaitingNeighboursNeighbourMessageDelayed testDriver;
    private Map<String, Boolean> isIterationMessageReceivedByAgentName;

    private final static Logger logger = Logger.getLogger(FinalProjectTests.Experiment.TestAgentsWaitingNeighboursNeighbourMessageDelayed.TestDataCollectionCommunicatorBehaviour.class);

    public TestDataCollectionCommunicatorBehaviour(TestAgentsWaitingNeighboursNeighbourMessageDelayed testAgentsWaitingForNeighbours) {
        this.testDriver = testAgentsWaitingForNeighbours;
        this.isIterationMessageReceivedByAgentName = new HashMap<>();
    }

    @Override
    public void action()
    {
        try
        {
            logger.info("started new iteration");
            if (!testDriver.initializationApplied)
            {
                logger.info("first iteration - applying agent initialization");
                testDriver.initializationApplied = true;
                testDriver.initializeAgents(myAgent);
                for (AgentData agentData : testDriver.problem.getAgentsData())
                {
                    isIterationMessageReceivedByAgentName.put(agentData.getName(), false);
                }
            }

            ACLMessage m;
            while ((m = myAgent.receive()) != null)
            {
                logger.info("receieved a message from " + m.getSender().getName());
                IterationCollectedData ICD = (IterationCollectedData) m.getContentObject();
                if (ICD.getIterNum() != this.currentIterationNumber)
                {
                    testDriver.notifyTestFailed(String.format("received a message from agent %s with iteration number %d where current iteration number is %d",
                            m.getSender().getLocalName(),
                            ICD.getIterNum(),
                            this.currentIterationNumber));
                }
                if (this.isIterationMessageReceivedByAgentName.get(m.getSender().getLocalName()))
                {
                    testDriver.notifyTestFailed("received more than one message in one iteration from agent " + m.getSender().getLocalName());
                }
                this.isIterationMessageReceivedByAgentName.put(m.getSender().getLocalName(), true);
            }
            if (didReceiveMessageFromAllAgents())
            {
                logger.info("received messages from all agents");
                if (this.currentIterationNumber < AbstractJadeIntegrationTest.MAXIMUM_ITERATIONS)
                {
                    logger.info("current iteration #"+ currentIterationNumber + " completed");
                    startNextIteration();
                }
                else
                {
                    logger.info("completed all iterations");
                    DFService.deregister(myAgent);
                    testDriver.notifyTestPassed(String.format("simulated %d iterations where all of the agents kept the same iteration", AbstractJadeIntegrationTest.MAXIMUM_ITERATIONS));
                }
            }
            block();
        }
        catch (Exception e)
        {
            testDriver.notifyTestFailed("an exception was thrown " + e.getMessage());
        }
    }

    private void startNextIteration()
    {
        for (AgentData agentData : testDriver.problem.getAgentsData())
        {
            isIterationMessageReceivedByAgentName.put(agentData.getName(), false);
        }
        this.currentIterationNumber++;
    }

    private boolean didReceiveMessageFromAllAgents()
    {
        return !isIterationMessageReceivedByAgentName.containsValue(false);
    }
}
