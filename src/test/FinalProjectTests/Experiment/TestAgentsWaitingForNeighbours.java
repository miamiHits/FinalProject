package FinalProjectTests.Experiment;

import FinalProject.BL.DataCollection.DataCollectionCommunicatorBehaviour;
import FinalProject.BL.IterationData.IterationCollectedData;
import FinalProject.BL.DataObjects.AgentData;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.lang.acl.ACLMessage;
import test.common.TestException;

import java.util.HashMap;
import java.util.Map;

public class TestAgentsWaitingForNeighbours extends AbstractJadeIntegrationTest
{
    private Map<String, AgentMessageType> messagesReceivedFromAgents;

    @Override
    public Behaviour load(Agent a) throws TestException
    {
        super.load(a);
        // Perform test specific initialisation
        this.messagesReceivedFromAgents = new HashMap<>();

        loadExperimentConfiguration();
        ExperimentTestUtils.publishTestAgentAsDataCollector(a);

        return new TestBehaviour_TestAgentsWaitingForNeighbours_DataCollectionCommunicatorBehaviour();// Create the Behaviour actually performing the test
    }
    public void clean(Agent a) {
        // Perform test specific clean-up
        super.clean(a);
    }

    @Override
    public void initializeAgents(Agent initializationAgent)
    {
        super.initializeAgents(initializationAgent);
        for (AgentData agentData : problem.getAgentsData())
        {
            messagesReceivedFromAgents.put(agentData.getName(), AgentMessageType.NONE);
        }
    }

    private class TestBehaviour_TestAgentsWaitingForNeighbours_DataCollectionCommunicatorBehaviour extends DataCollectionCommunicatorBehaviour
    {
        private int currentIterationNumber = 0;

        @Override
        public void action()
        {
            try
            {
                if (!initializationApplied)
                {
                    initializationApplied = true;
                    initializeAgents(myAgent);
                }

                ACLMessage m;
                while ((m = myAgent.receive()) != null)
                {
                    IterationCollectedData ICD = (IterationCollectedData) m.getContentObject();
                    examineMessage(ICD, m);
                    if (ICD.getIterNum() != this.currentIterationNumber)
                    {
                        failed(String.format("received a message from agent %s with iteration number %d where current iteration number is %d",
                                m.getSender().getLocalName(),
                                ICD.getIterNum(),
                                this.currentIterationNumber));
                    }
                    AgentMessageType updatedType = ICD.getePeak() == -1 ? AgentMessageType.NO_EPEAK : AgentMessageType.WITH_EPEAK;
                    messagesReceivedFromAgents.put(m.getSender().getLocalName(),updatedType);
                }
                if (didReceiveMessageFromAllAgents())
                {
                    if (this.currentIterationNumber < MAXIMUM_ITERATIONS)
                    {
                        startNextIteration();
                    }
                    else
                    {
                        DFService.deregister(myAgent);
                        passed(String.format("simulated %d iterations where all of the agents kept the same iteration", MAXIMUM_ITERATIONS));
                    }
                }
                block();
            }
            catch (Exception e)
            {
                failed("an exception was thrown " + e.getMessage());
            }
        }

        private void startNextIteration()
        {
            ExperimentTestUtils.sendCSumToAllAgents(messagesReceivedFromAgents.keySet(), 10, myAgent);
            if (!messagesReceivedFromAgents.containsValue(AgentMessageType.NO_EPEAK))
            {//this is a new iteration since all epeak messages from all agents were received
                messagesReceivedFromAgents.replaceAll((k, v) -> AgentMessageType.NONE);
                this.currentIterationNumber++;
                System.out.println("starting iteration #" + this.currentIterationNumber);
            }
        }

        private boolean didReceiveMessageFromAllAgents()
        {
            return !messagesReceivedFromAgents.containsValue(AgentMessageType.NONE);
        }

        private void examineMessage(IterationCollectedData ICD, ACLMessage m)
        {
            if (ICD.getePeak() == -1)
            {//this is a message of end of iteration prior the epeak calculation
                if (messagesReceivedFromAgents.get(m.getSender().getLocalName()) == AgentMessageType.NO_EPEAK)
                {
                    failed("agent " + m.getSender().getLocalName() + " sent more than once the end of iteration, prior epeak calculation message");
                }
            }
            else
            {//this is a message that should be sent once all agent
                if (messagesReceivedFromAgents.get(m.getSender().getLocalName()) == AgentMessageType.WITH_EPEAK)
                {
                    failed("agent " + m.getSender().getLocalName() + " sent more than once the end of iteration, after epeak calculation message");
                }
            }
        }
    }
}
