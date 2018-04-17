package FinalProjectTests.Experiment.TestAgentsWaitingNeighboursNeighbourMessageDelayed;

import FinalProject.BL.DataCollection.DataCollectionCommunicatorBehaviour;
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

    private final static Logger logger = Logger.getLogger(FinalProjectTests.Experiment.TestAgentsWaitingForNeighbours.TestDataCollectionCommunicatorBehaviour.class);

    public TestDataCollectionCommunicatorBehaviour(TestAgentsWaitingNeighboursNeighbourMessageDelayed testAgentsWaitingForNeighbours) {
        this.testDriver = testAgentsWaitingForNeighbours;
        this.isIterationMessageReceivedByAgentName = new HashMap<>();
        for (Map.Entry<String, Boolean> entry : isIterationMessageReceivedByAgentName.entrySet()) {
            isIterationMessageReceivedByAgentName.put(entry.getKey(), false);
        }
    }

    @Override
    public void action()
    {
        try
        {
            if (!testDriver.initializationApplied)
            {
                testDriver.initializationApplied = true;
                testDriver.initializeAgents(myAgent);
            }

            ACLMessage m;
            while ((m = myAgent.receive()) != null)
            {
                IterationCollectedData ICD = (IterationCollectedData) m.getContentObject();
                if (ICD.getIterNum() != this.currentIterationNumber)
                {
                    testDriver.notifyTestFailed(String.format("received a message from agent %s with iteration number %d where current iteration number is %d",
                            m.getSender().getLocalName(),
                            ICD.getIterNum(),
                            this.currentIterationNumber));
                }
            }
            if (didReceiveMessageFromAllAgents())
            {
                if (this.currentIterationNumber < AbstractJadeIntegrationTest.MAXIMUM_ITERATIONS)
                {
                    startNextIteration();
                }
                else
                {
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
        for (Map.Entry<String, Boolean> entry : isIterationMessageReceivedByAgentName.entrySet()) {
            isIterationMessageReceivedByAgentName.put(entry.getKey(), false);
        }
        this.currentIterationNumber++;
    }

    private boolean didReceiveMessageFromAllAgents()
    {
        return !isIterationMessageReceivedByAgentName.containsValue(false);
    }
}
