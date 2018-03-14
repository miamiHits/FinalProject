package FinalProjectTests.Experiment;

import FinalProject.BL.Agents.DSA;
import FinalProject.BL.Agents.SmartHomeAgent;
import FinalProject.BL.DataCollection.DataCollectionCommunicator;
import FinalProject.BL.DataCollection.DataCollectionCommunicatorBehaviour;
import FinalProject.BL.DataObjects.AgentData;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class TestAgentsWaitingForDataCollector extends AbstractJadeIntegrationTest
{
    private Map<String, TestAgentsWaitingForNeighbours.AgentMessageType> messagesReceivedFromAgents;
    private AtomicBoolean testHomeAgentShouldNeighbourReceiveMessage;

    private static int currentIterationNumber = 0;


    @Override
    public Behaviour load(Agent a) throws TestException
    {
        super.load(a);
        // Perform test specific initialisation
        this.messagesReceivedFromAgents = new HashMap<>();
        this.testHomeAgentShouldNeighbourReceiveMessage = new AtomicBoolean(false);

        loadExperimentConfiguration();
        ExperimentTestUtils.publishTestAgentAsDataCollector(a);

        return new TestBehaviour_TestAgentsWaitingForDataCollector_DataCollectionCommunicatorBehaviour();
    }
    public void clean(Agent a) {
        // Perform test specific clean-up
        super.clean(a);
    }

    @Override
    public void initializeAgents(Agent initializationAgent)
    {
        // override the behaviour of the first agent
        List<AgentData> allAgents = problem.getAgentsData();
        AgentData overriddenAgentData = allAgents.get(0);
        allAgents.remove(0);
        problem.setAllHomes(allAgents);

        Object[] agentInitializationArgs = new Object[4];
        agentInitializationArgs[0] = new TestBehaviour_TestAgentsWaitingForDataCollector_DSA();
        agentInitializationArgs[1] = overriddenAgentData;
        agentInitializationArgs[2] = algorithm.getBehaviourName();
        agentInitializationArgs[3] = problem.getId();
        try
        {
            AgentController agentController = initializationAgent.getContainerController().createNewAgent(overriddenAgentData.getName(),
                    SmartHomeAgent.class.getName(),
                    agentInitializationArgs);
            super.initializeAgents(initializationAgent);
            agentController.start();
        } catch (StaleProxyException e)
        {
            e.printStackTrace();
        }


        for (AgentData agentData : problem.getAgentsData())
        {
            messagesReceivedFromAgents.put(agentData.getName(), AgentMessageType.NONE);
        }
        messagesReceivedFromAgents.put(overriddenAgentData.getName(), AgentMessageType.NONE);
    }

    private class TestBehaviour_TestAgentsWaitingForDataCollector_DataCollectionCommunicatorBehaviour extends DataCollectionCommunicatorBehaviour
    {


        @Override
        public void action()
        {
            try
            {
                if (!initializationApplied)
                {
                    testHomeAgentShouldNeighbourReceiveMessage.set(true);
                    initializationApplied = true;
                    initializeAgents(myAgent);
                }

                ACLMessage m;
                while ((m = myAgent.receive()) != null)
                {
                    System.out.println("test data collector received a message from " + m.getSender().getLocalName());
                    IterationCollectedData ICD = (IterationCollectedData) m.getContentObject();
                    examineMessage(ICD, m);
                    if (ICD.getIterNum() != currentIterationNumber)
                    {
                        failed(String.format("received a message from agent %s with iteration number %d where current iteration number is %d",
                                m.getSender().getLocalName(),
                                ICD.getIterNum(),
                                currentIterationNumber));
                    }
                    TestAgentsWaitingForNeighbours.AgentMessageType updatedType = ICD.getEpeak() == -1 ? TestAgentsWaitingForNeighbours.AgentMessageType.NO_EPEAK : TestAgentsWaitingForNeighbours.AgentMessageType.WITH_EPEAK;
                    messagesReceivedFromAgents.put(m.getSender().getLocalName(), updatedType);

                    if (didReceiveFirstMessageFromAllAgents())
                    {
                        sendCSumToAllAgents();
                    }
                    if (didReceiveSecondMessageFromAllAgents())
                    {
                        if (currentIterationNumber < MAXIMUM_ITERATIONS)
                        {
                            startNextIteration();
                        }
                        else
                        {
                            DFService.deregister(myAgent);
                            passed(String.format("simulated %d iterations where all of the agents kept the same iteration", MAXIMUM_ITERATIONS));
                        }

                    }
                }
                block();
            }
            catch (Exception e)
            {
                failed("an exception was thrown " + e.getMessage());
            }
        }

        private void sendCSumToAllAgents()
        {
            System.out.println("test DC about to sleep for 1 sec");
            try
            {//create a delay, making sure the other agents don't start the next iteration until the message was sent
                Thread.sleep(1000);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            System.out.println("test DC about to send csum to all agents");
            testHomeAgentShouldNeighbourReceiveMessage.set(true);
            ExperimentTestUtils.sendCSumToAllAgents(messagesReceivedFromAgents.keySet(), 10, myAgent);
        }

        private void startNextIteration()
        {
            messagesReceivedFromAgents.replaceAll((k, v) -> AgentMessageType.NONE);
            currentIterationNumber++;
            System.out.println("starting iteration #" + currentIterationNumber);
//            sendCSumToAllAgents();
        }

        /**
         *
         * @return true if the data collector received the second message for the current iteration
         */
        private boolean didReceiveSecondMessageFromAllAgents()
        {
            return messagesReceivedFromAgents
                    .values()
                    .stream()
                    .filter( messageType -> messageType == AgentMessageType.WITH_EPEAK)
                    .count() == messagesReceivedFromAgents.size();
        }

        /**
         *
         * @return true if the data collector received the second message for the current iteration
         */
        private boolean didReceiveFirstMessageFromAllAgents()
        {
            return messagesReceivedFromAgents
                    .values()
                    .stream()
                    .filter( messageType -> messageType == AgentMessageType.NO_EPEAK)
                    .count() == messagesReceivedFromAgents.size();
        }


        private void examineMessage(IterationCollectedData ICD, ACLMessage m)
        {
            if (ICD.getEpeak() == -1)
            {//this is a message of end of iteration prior the epeak calculation
                if (messagesReceivedFromAgents.get(m.getSender().getLocalName()) == TestAgentsWaitingForNeighbours.AgentMessageType.NO_EPEAK)
                {
                    failed("agent " + m.getSender().getLocalName() + " sent more than once the end of iteration, prior epeak calculation message");
                }
            }
            else
            {//this is a message that should be sent once all agent
                if (messagesReceivedFromAgents.get(m.getSender().getLocalName()) == TestAgentsWaitingForNeighbours.AgentMessageType.WITH_EPEAK)
                {
                    failed("agent " + m.getSender().getLocalName() + " sent more than once the end of iteration, after epeak calculation message");
                }
            }
        }
    }

    private class TestBehaviour_TestAgentsWaitingForDataCollector_DSA extends DSA
    {
        private void sendFakeIterationToCollector()
        {
            System.out.println("test home agent sends fake iteration data to the data collector");
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType(DataCollectionCommunicator.SERVICE_TYPE);

            template.addServices(sd);

            try {
                DFAgentDescription[] result = DFService.search(this.agent, template);
                if (result.length > 0)
                {
                    ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
                    message.setOntology(agent.getProblemId()+agent.getAlgoId());
                    for (DFAgentDescription foundAID : result)
                    {
                        message.addReceiver(foundAID.getName());
                    }
                    message.setContentObject(this.agentIterationCollected);
                    agent.send(message);
                }
                else
                {
                    failed("test home agent could not find test data collector");
                }
            }
            catch (FIPAException | IOException | NullPointerException e) {
                e.printStackTrace();
                failed("test home agent failed sending a message to the test data collector");
            }

        }

        private void sendFakeIterationToNeighbors()
        {
            System.out.println("test home agent sends fake iteration data to its neighbours");
            ACLMessage aclmsg = new ACLMessage(ACLMessage.REQUEST);
            agent.getAgentData().getNeighbors().stream()
                    .map(neighbor -> new AID(neighbor.getName(), AID.ISLOCALNAME))
                    .forEach(aclmsg::addReceiver);

            try {
                System.out.println("test home agent about to send the message " + aclmsg.toString());
                aclmsg.setContentObject(this.agentIterationCollected);
                agent.send(aclmsg);
            } catch (IOException e) {
                failed("test home agent failed sending the fake iteration data to it's neighbours");
            }
        }

        private void collectNeighboursMessages()
        {
            List<ACLMessage> messages = new ArrayList<>();
            ACLMessage receivedMessage;
            int neighbourCount = this.agent.getAgentData().getNeighbors().size();
            while (messages.size() < neighbourCount)//the additional one is for the data collector's message
            {
                receivedMessage = this.agent.blockingReceive(SmartHomeAgent.MESSAGE_TEMPLATE_SENDER_IS_NEIGHBOUR);
                messages.add(receivedMessage);
                System.out.println("test home agent received message from " + receivedMessage.getSender().getLocalName());
            }
            testHomeAgentShouldNeighbourReceiveMessage.set(false);
        }

        /**
         * makes sure no messages are sent to the test home agent
         */
        private void verifyNeighboursDidNotStartNewIteration()
        {
            if (!testHomeAgentShouldNeighbourReceiveMessage.get())
            {
                ACLMessage invalidNeighbourMessage = this.agent.blockingReceive(SmartHomeAgent.MESSAGE_TEMPLATE_SENDER_IS_NEIGHBOUR, 1000);
                if (invalidNeighbourMessage != null)
                {
                    failed(String.format("%s neighbour started a new iteration and sent the test home agent its next iteration schedule", invalidNeighbourMessage.getSender().getLocalName()));
                }
            }
        }

        @Override
        public void action()
        {
            System.out.println("test home agent starts new iteration");
            this.collectNeighboursMessages();
            verifyNeighboursDidNotStartNewIteration();
            this.agentIterationCollected = new IterationCollectedData(
                    currentIterationNumber,
                    agent.getName(),
                    10,
                    new double[12],
                    agent.getProblemId(),
                    agent.getAlgoId(),
                    (agent.getAgentData().getNeighbors().stream().map(AgentData::getName).collect(Collectors.toSet())),
                    -1);

            this.sendFakeIterationToNeighbors();
            this.verifyNeighboursDidNotStartNewIteration();
            this.sendFakeIterationToCollector();
            this.verifyNeighboursDidNotStartNewIteration();
            if (currentNumberOfIter > MAXIMUM_ITERATIONS)
            {
                return;
            }
            this.agent.blockingReceive(SmartHomeAgent.MESSAGE_TEMPLATE_SENDER_IS_COLLECTOR);
            this.agentIterationCollected = new IterationCollectedData(
                    currentIterationNumber,
                    agent.getName(),
                    10,
                    new double[12],
                    agent.getProblemId(),
                    agent.getAlgoId(),
                    (agent.getAgentData().getNeighbors().stream().map(AgentData::getName).collect(Collectors.toSet())),
                    12);
            this.sendFakeIterationToNeighbors();
            this.verifyNeighboursDidNotStartNewIteration();
            this.sendFakeIterationToCollector();
            this.verifyNeighboursDidNotStartNewIteration();
        }

        @Override
        public boolean done() {
            if (currentIterationNumber > MAXIMUM_ITERATIONS)
            {
                this.myAgent.doDelete();
                return true;
            }
            return false;
        }


    }
}
