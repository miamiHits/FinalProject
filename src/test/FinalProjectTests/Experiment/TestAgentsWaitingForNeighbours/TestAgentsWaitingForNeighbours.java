package FinalProjectTests.Experiment.TestAgentsWaitingForNeighbours;

import FinalProject.BL.DataObjects.AgentData;
import FinalProjectTests.Experiment.AbstractJadeIntegrationTest;
import FinalProjectTests.Experiment.ExperimentTestUtils;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import org.apache.log4j.Logger;
import test.common.TestException;

import java.util.HashMap;
import java.util.Map;

public class TestAgentsWaitingForNeighbours extends AbstractJadeIntegrationTest
{
    private Map<String, AgentMessageType> messagesReceivedFromAgents;

    private final static Logger logger = Logger.getLogger(TestAgentsWaitingForNeighbours.class);

    @Override
    public Behaviour load(Agent a) throws TestException
    {
        super.load(a);
        // Perform test specific initialisation
        this.messagesReceivedFromAgents = new HashMap<>();

        loadExperimentConfiguration();
        ExperimentTestUtils.publishTestAgentAsDataCollector(a);

        return new FinalProjectTests.Experiment.TestAgentsWaitingForNeighbours.
                TestDataCollectionCommunicatorBehaviour(this, messagesReceivedFromAgents);// Create the Behaviour actually performing the test
    }
    public void clean(Agent a) {
        // Perform test specific clean-up
        super.clean(a);
    }

    public void initializeAgents(Agent initializationAgent)
    {
        super.initializeAgents(initializationAgent, this.algorithm, null);
        for (AgentData agentData : problem.getAgentsData())
        {
            messagesReceivedFromAgents.put(agentData.getName(), AgentMessageType.NONE);
        }
    }


}
