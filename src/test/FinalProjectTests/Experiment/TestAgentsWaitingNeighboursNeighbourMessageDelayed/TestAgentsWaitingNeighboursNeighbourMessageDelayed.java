package FinalProjectTests.Experiment.TestAgentsWaitingNeighboursNeighbourMessageDelayed;

import FinalProject.BL.DataObjects.AgentData;
import FinalProjectTests.Experiment.AbstractJadeIntegrationTest;
import FinalProjectTests.Experiment.ExperimentTestUtils;
import FinalProjectTests.Experiment.TestAgentsWaitingForNeighbours.TestDataCollectionCommunicatorBehaviour;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import test.common.TestException;

import java.util.HashMap;

public class TestAgentsWaitingNeighboursNeighbourMessageDelayed extends AbstractJadeIntegrationTest {

    @Override
    public Behaviour load(Agent a) throws TestException
    {
        super.load(a);

        loadExperimentConfiguration();
        ExperimentTestUtils.publishTestAgentAsDataCollector(a);

        return new FinalProjectTests.Experiment.TestAgentsWaitingNeighboursNeighbourMessageDelayed.
                TestDataCollectionCommunicatorBehaviour(this);// Create the Behaviour actually performing the test
    }
    public void clean(Agent a) {
        // Perform test specific clean-up
        super.clean(a);
    }
}
