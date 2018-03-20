package FinalProjectTests.Experiment;

import FinalProject.BL.Agents.DSA;
import FinalProject.BL.Agents.SmartHomeAgent;
import FinalProject.BL.DataCollection.DataCollectionCommunicator;
import FinalProject.BL.DataObjects.AgentData;
import FinalProject.BL.Experiment;
import FinalProject.BL.IterationData.IterationCollectedData;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;
import test.common.TestException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.mockito.Mockito.mock;

public class TestAgentReceivingMessagesFromDataCollector extends FinalProjectTests.Experiment.AbstractJadeIntegrationTest
{
    private static int currentIterationNumber = 0;
    private AgentData overriddenAgentData;
    private Agent testAgent;


    @Override
    public Behaviour load(Agent a) throws TestException
    {
        super.load(a);
        this.testAgent = a;
        super.loadExperimentConfiguration();

        initializeAgents(a);

        return new TestBehaviour_TestAgentReceivingMessagesFromDataCollector_DSA();
    }

    public void clean(Agent a)
    {
        // Perform test specific clean-up
        super.clean(a);
    }

    @Override
    public void initializeAgents(Agent initializationAgent)
    {
        try
        {
            initializeDataCollector(initializationAgent);

            // override the behaviour of the first agent
            List<AgentData> allAgents = problem.getAgentsData();
            this.overriddenAgentData = allAgents.get(0);
            String originalName = this.overriddenAgentData.getName();
            String newName = initializationAgent.getLocalName();
            allAgents.remove(0);
            for (AgentData agentData : allAgents)
            {
                for (AgentData neighbour : agentData.getNeighbors())
                {
                    if (neighbour.getName().equals(originalName))
                    {
                        neighbour.setName(newName);
                    }
                }

            }
            this.overriddenAgentData.setName(newName);
            problem.setAllHomes(allAgents);

            registerAgent();

            super.initializeAgents(initializationAgent);
        } catch (StaleProxyException e)
        {
            failed("could not initialize test");
            e.printStackTrace();
        }

    }

    private void initializeDataCollector(Agent initializationAgent) throws StaleProxyException
    {
        Experiment mockExperiment = mock(Experiment.class);

        Experiment.maximumIterations = MAXIMUM_ITERATIONS;

        Map<String, Integer> numOfAgentsInProblems = new HashMap<>();
        Map<String, double[]> prices = new HashMap<>();
        numOfAgentsInProblems.put(problem.getId(), problem.getAgentsData().size());
        prices.put(problem.getId(), problem.getPriceScheme());

        Object[] collectorInitializationArgs = new Object[3];
        collectorInitializationArgs[0] = numOfAgentsInProblems;
        collectorInitializationArgs[1] = prices;
        collectorInitializationArgs[2] = mockExperiment;
        AgentController dataCollectorController = initializationAgent.getContainerController().createNewAgent(DataCollectionCommunicator.SERVICE_NAME,
                DataCollectionCommunicator.class.getName(),
                collectorInitializationArgs);
        dataCollectorController.start();
    }

    private void registerAgent()
    {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(testAgent.getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(SmartHomeAgent.SERVICE_TYPE);
        sd.setName(SmartHomeAgent.SERVICE_NAME);
        sd.addOntologies(problem.getId()+algorithm.getBehaviourName());
        dfd.addServices(sd);
        try
        {
            DFService.register(testAgent, dfd);

        }
        catch (FIPAException e)
        {
            e.printStackTrace();
            failed("could not register the test home agent");
        }

    }


/////////////////////////////////////////////////////////////////////
///// TestBehaviour_TestAgentReceivingMessagesFromDataCollector_DSA
/////////////////////////////////////////////////////////////////////

    private class TestBehaviour_TestAgentReceivingMessagesFromDataCollector_DSA extends DSA
    {
        private void sendFakeIterationToCollector()
        {
            System.out.println("test home agent sends fake iteration data to the data collector");
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType(DataCollectionCommunicator.SERVICE_TYPE);

            template.addServices(sd);

            try
            {
                DFAgentDescription[] result = DFService.search(testAgent, template);
                if (result.length > 0)
                {
                    ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
                    message.setOntology(problem.getId() + algorithm.getBehaviourName());
                    for (DFAgentDescription foundAID : result)
                    {
                        message.addReceiver(foundAID.getName());
                    }
                    message.setContentObject(this.agentIterationCollected);
                    testAgent.send(message);
                } else
                {
                    failed("test home agent could not find test data collector");
                }
            } catch (FIPAException | IOException | NullPointerException e)
            {
                e.printStackTrace();
                failed("test home agent failed sending a message to the test data collector");
            }

        }

        private void sendFakeIterationToNeighbors()
        {
            System.out.println(String.format("test home agent sends fake iteration #%d data to its neighbours", this.agentIterationCollected.getIterNum()));
            ACLMessage aclmsg = new ACLMessage(ACLMessage.REQUEST);
            overriddenAgentData.getNeighbors().stream()
                    .map(neighbor -> new AID(neighbor.getName(), AID.ISLOCALNAME))
                    .forEach(aclmsg::addReceiver);

            try
            {
                System.out.println("test home agent about to send the message " + aclmsg.toString());
                aclmsg.setContentObject(this.agentIterationCollected);
                testAgent.send(aclmsg);
            } catch (IOException e)
            {
                failed("test home agent failed sending the fake iteration data to it's neighbours");
            }
        }

        private void collectNeighboursMessages()
        {
            List<ACLMessage> messages = new ArrayList<>();
            ACLMessage receivedMessage;
            int neighbourCount = overriddenAgentData.getNeighbors().size();
            while (messages.size() < neighbourCount)//the additional one is for the data collector's message
            {
                receivedMessage = testAgent.blockingReceive(SmartHomeAgent.MESSAGE_TEMPLATE_SENDER_IS_NEIGHBOUR);
                messages.add(receivedMessage);
                System.out.println("test home agent received message from " + receivedMessage.getSender().getLocalName());
            }
        }

        @Override
        public void action()
        {
            System.out.println("test home agent starts new iteration");
            this.collectNeighboursMessages();
            this.agentIterationCollected = new IterationCollectedData(
                    currentIterationNumber,
                    overriddenAgentData.getName(),
                    10,
                    new double[12],
                    problem.getId(),
                    algorithm.getBehaviourName(),
                    (overriddenAgentData.getNeighbors().stream().map(AgentData::getName).collect(Collectors.toSet())),
                    -1, 1, 1);

            this.sendFakeIterationToNeighbors();
            this.sendFakeIterationToCollector();
            if (currentNumberOfIter > MAXIMUM_ITERATIONS)
            {
                return;
            }
            ACLMessage receivedMessage = testAgent.blockingReceive(SmartHomeAgent.MESSAGE_TEMPLATE_SENDER_IS_COLLECTOR, 10000);
            if (receivedMessage == null)
            {
                failed("the data collector did not send the required message within the time limit");
            }
            this.agentIterationCollected = new IterationCollectedData(
                    currentIterationNumber,
                    overriddenAgentData.getName(),
                    10,
                    new double[12],
                    problem.getId(),
                    algorithm.getBehaviourName(),
                    (overriddenAgentData.getNeighbors().stream().map(AgentData::getName).collect(Collectors.toSet())),
                    12, 1, 1);
            this.sendFakeIterationToNeighbors();
            this.sendFakeIterationToCollector();
            currentIterationNumber++;
        }

        @Override
        public boolean done()
        {
            if (currentIterationNumber > MAXIMUM_ITERATIONS)
            {
                try
                {
                    DFService.deregister(testAgent);
                } catch (FIPAException e)
                {
                    e.printStackTrace();
                }
                passed("success");
                return true;
            }
            return false;
        }


    }
}

