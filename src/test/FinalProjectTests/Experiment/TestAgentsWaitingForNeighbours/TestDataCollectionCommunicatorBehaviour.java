package FinalProjectTests.Experiment.TestAgentsWaitingForNeighbours;

import FinalProject.BL.DataCollection.DataCollectionCommunicatorBehaviour;
import FinalProject.BL.IterationData.IterationCollectedData;
import FinalProjectTests.Experiment.AbstractJadeIntegrationTest;
import FinalProjectTests.Experiment.ExperimentTestUtils;
import jade.domain.DFService;
import jade.lang.acl.ACLMessage;
import org.apache.log4j.Logger;

import java.util.Map;

public class TestDataCollectionCommunicatorBehaviour extends DataCollectionCommunicatorBehaviour{
    private int currentIterationNumber = 0;
    TestAgentsWaitingForNeighbours testDriver;
    private Map<String, AbstractJadeIntegrationTest.AgentMessageType> messagesReceivedFromAgents;

    private final static Logger logger = Logger.getLogger(FinalProjectTests.Experiment.TestAgentsWaitingForNeighbours.TestDataCollectionCommunicatorBehaviour.class);

    public TestDataCollectionCommunicatorBehaviour(TestAgentsWaitingForNeighbours testAgentsWaitingForNeighbours, Map<String, AbstractJadeIntegrationTest.AgentMessageType> messagesReceivedFromAgents) {
        this.testDriver = testAgentsWaitingForNeighbours;
        this.messagesReceivedFromAgents = messagesReceivedFromAgents;
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
                examineMessage(ICD, m);
                if (ICD.getIterNum() != this.currentIterationNumber)
                {
                    testDriver.notifyTestFailed(String.format("received a message from agent %s with iteration number %d where current iteration number is %d",
                            m.getSender().getLocalName(),
                            ICD.getIterNum(),
                            this.currentIterationNumber));
                }
                AbstractJadeIntegrationTest.AgentMessageType updatedType = ICD.getePeak() == -1 ? AbstractJadeIntegrationTest.AgentMessageType.NO_EPEAK : AbstractJadeIntegrationTest.AgentMessageType.WITH_EPEAK;
                messagesReceivedFromAgents.put(m.getSender().getLocalName(),updatedType);
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
        ExperimentTestUtils.sendCSumToAllAgents(messagesReceivedFromAgents.keySet(), 10, myAgent);
        if (!messagesReceivedFromAgents.containsValue(AbstractJadeIntegrationTest.AgentMessageType.NO_EPEAK))
        {//this is a new iteration since all epeak messages from all agents were received
            messagesReceivedFromAgents.replaceAll((k, v) -> AbstractJadeIntegrationTest.AgentMessageType.NONE);
            this.currentIterationNumber++;
            logger.info("starting iteration #" + this.currentIterationNumber);
        }
    }

    private boolean didReceiveMessageFromAllAgents()
    {
        return !messagesReceivedFromAgents.containsValue(AbstractJadeIntegrationTest.AgentMessageType.NONE);
    }

    private void examineMessage(IterationCollectedData ICD, ACLMessage m)
    {
        if (ICD.getePeak() == -1)
        {//this is a message of end of iteration prior the epeak calculation
            if (messagesReceivedFromAgents.get(m.getSender().getLocalName()) == AbstractJadeIntegrationTest.AgentMessageType.NO_EPEAK)
            {
                testDriver.notifyTestFailed("agent " + m.getSender().getLocalName() + " sent more than once the end of iteration, prior epeak calculation message");
            }
        }
        else
        {//this is a message that should be sent once all agent
            if (messagesReceivedFromAgents.get(m.getSender().getLocalName()) == AbstractJadeIntegrationTest.AgentMessageType.WITH_EPEAK)
            {
                testDriver.notifyTestFailed("agent " + m.getSender().getLocalName() + " sent more than once the end of iteration, after epeak calculation message");
            }
        }
    }

}
