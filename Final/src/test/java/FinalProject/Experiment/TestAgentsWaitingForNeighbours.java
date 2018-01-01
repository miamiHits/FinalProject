package FinalProject.Experiment;

import FinalProject.BL.Agents.DSA;
import FinalProject.BL.Agents.SmartHomeAgent;
import FinalProject.BL.Agents.SmartHomeAgentBehaviour;
import FinalProject.BL.DataCollection.DataCollectionCommunicator;
import FinalProject.BL.DataCollection.DataCollectionCommunicatorBehaviour;
import FinalProject.BL.Experiment;
import FinalProject.BL.IterationData.IterationCollectedData;
import FinalProject.BL.Problems.AgentData;
import FinalProject.BL.Problems.Problem;
import FinalProject.DAL.*;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import test.common.Test;
import test.common.TestException;
import test.common.TestUtility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// this test will
public class TestAgentsWaitingForNeighbours extends Test
{
    private SmartHomeAgentBehaviour algorithm;
    private Problem problem;
    private Map<String, Boolean> messagesReceivedFromAgents;

    public static int MAXIMUM_ITERATIONS = 2;

    public Behaviour load(Agent a) throws TestException
    {
        org.apache.log4j.BasicConfigurator.configure();
        // Perform test specific initialisation
        this.messagesReceivedFromAgents = new HashMap<>();

        loadExpeimentConfiguration();
        publishTestAgentAsDataCollector(a);

        Behaviour b = new TestBehaviour();// Create the Behaviour actually performing the test
        return b;
    }
    public void clean(Agent a) {
        // Perform test specific clean-up
    }

    private void loadExpeimentConfiguration()
    {
        List<String> algoNameList = new ArrayList<>();
        algoNameList.add(DSA.class.getName());
        List<String> problemNameList = new ArrayList<>();
        problemNameList.add("dm_7_1_2");
        JsonLoaderInterface jsonLoader = new JsonLoader("Final\\src\\test\\testResources\\jsons");
        AlgoLoaderInterface algorithmLoader = new AlgorithmLoader("Final\\target\\classes\\FinalProject\\BL\\Agents");
        DataAccessController dal = new DataAccessController(jsonLoader, algorithmLoader);
        algorithm =  dal.getAlgorithms(algoNameList).get(0);
        problem = dal.getProblems(problemNameList).get(0);
        Experiment.maximumIterations = MAXIMUM_ITERATIONS;
    }
    public void publishTestAgentAsDataCollector(Agent a)
    {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(a.getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(DataCollectionCommunicator.SERVICE_TYPE);
        sd.setName(DataCollectionCommunicator.SERVICE_NAME);
        dfd.addServices(sd);
        try
        {
            DFService.register(a, dfd);
        } catch (FIPAException e)
        {
            e.printStackTrace();
        }
    }

    private void initializeAgents(Agent agent)
    {
        for (AgentData agentData : problem.getAgentsData())
        {
            Object[] agentInitializationArgs = new Object[4];
            agentInitializationArgs[0] = algorithm.cloneBehaviour();
            agentInitializationArgs[1] = agentData;
            agentInitializationArgs[2] = algorithm.getBehaviourName();
            agentInitializationArgs[3] = problem.getId();
            try
            {
                TestUtility.createAgent(agent, agentData.getName(),
                        SmartHomeAgent.class.getName(),
                        agentInitializationArgs);
            } catch (TestException e)
            {
                e.printStackTrace();
            }
            messagesReceivedFromAgents.put(agentData.getName(), false);
        }
    }

    private class TestBehaviour extends DataCollectionCommunicatorBehaviour
    {
        private boolean firstRun = false;
        private int currentIterationNumber = 0;

        @Override
        public void action()
        {
            try
            {
                if (!firstRun)
                {
                    firstRun = true;
                    initializeAgents(myAgent);
                }

                ACLMessage m;
                while ((m = myAgent.receive()) != null)
                {
                    IterationCollectedData IDC = (IterationCollectedData) m.getContentObject();
                    if (messagesReceivedFromAgents.get(m.getSender().getLocalName()))
                    {//received more than one message from the agent for the current
                        failed("received more than one message from an agent");
                    }
                    if (IDC.getIterNum() != this.currentIterationNumber)
                    {
                        failed(String.format("received a message from agent %s with iteration number %d where current iteration number is %d",
                                m.getSender().getLocalName(),
                                IDC.getIterNum(),
                                this.currentIterationNumber));
                    }
                    messagesReceivedFromAgents.put(m.getSender().getLocalName(),true);
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
            messagesReceivedFromAgents.replaceAll((k, v) -> false);
            this.currentIterationNumber++;
            for (String agentName : messagesReceivedFromAgents.keySet())
            {
                ACLMessage replay = new ACLMessage(ACLMessage.INFORM);
                replay.addReceiver(new AID(agentName, false));
                replay.setContent(String.valueOf(10));
                myAgent.send(replay);
            }
            System.out.println("starting iteration #" + this.currentIterationNumber);
        }

        private boolean didReceiveMessageFromAllAgents()
        {
            return !messagesReceivedFromAgents.containsValue(false);
        }
    }
}
