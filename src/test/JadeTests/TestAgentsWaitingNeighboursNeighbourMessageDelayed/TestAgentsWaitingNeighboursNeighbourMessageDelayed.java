package JadeTests.TestAgentsWaitingNeighboursNeighbourMessageDelayed;

import FinalProject.BL.DataObjects.AgentData;
import JadeTests.AbstractJadeIntegrationTest;
import JadeTests.ExperimentTestUtils;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import org.apache.log4j.Logger;
import test.common.TestException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestAgentsWaitingNeighboursNeighbourMessageDelayed extends AbstractJadeIntegrationTest {

    public static final int MESSAGE_DELAY_IN_MILLISEC = 20000;

    private final static Logger logger = Logger.getLogger(TestAgentsWaitingNeighboursNeighbourMessageDelayed.class);

    @Override
    public Behaviour load(Agent a) throws TestException
    {
        super.load(a);

        loadExperimentConfiguration();
        ExperimentTestUtils.publishTestAgentAsDataCollector(a);

        return new TestDataCollectionCommunicatorBehaviour(this);// Create the Behaviour actually performing the test
    }
    public void clean(Agent a) {
        // Perform test specific clean-up
        super.clean(a);
    }

    public void initializeAgents(Agent initializationAgent)
    {
        logger.info("initializing agents");
        List<AgentData> allAgentsData = problem.getAgentsData();
        initializeSpecificTestAgent(initializationAgent, 0, 2);
        initializeSpecificTestAgent(initializationAgent, 1, 3);
        initializeSpecificTestAgent(initializationAgent, 2, 5);
        super.initializeAgents(initializationAgent, this.algorithm, allAgentsData.subList(3, allAgentsData.size()), null);
    }

    private void initializeSpecificTestAgent(Agent initializationAgent, int agentIndex, int agentDelayInterval)
    {
        AgentData agentData = problem.getAgentsData().get(agentIndex);
        agentData.setName(agentData.getName() + "-delay-every-" + agentDelayInterval);
        TestDSAAgent algorithm = new TestDSAAgent(this, agentDelayInterval);
        super.initializeAgents(initializationAgent, algorithm, new ArrayList<AgentData>(Arrays.asList(agentData)), null);
    }

}
