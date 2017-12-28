package FinalProject.BL.Agents;


import FinalProject.BL.DataCollection.DataCollectionCommunicator;
import FinalProject.BL.IterationData.AgentIterationData;
import FinalProject.BL.IterationData.IterationCollectedData;
import FinalProject.Utils;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class SmartHomeAgentBehaviour extends Behaviour {

    public  String agentName;
    public SmartHomeAgent agent;
    protected int currentNumberOfIter;
    protected int FINAL_TICK;
    protected AlgorithmDataHelper helper;
    protected AgentIterationData agentIterationData;
    protected IterationCollectedData agentIteraionCollected;

    private final static Logger logger = Logger.getLogger(SmartHomeAgentBehaviour.class);

    protected abstract void doIteration();

    @Override
    public void action() {
        logger.info("Starting work on Iteration: " + agent.getIterationNum());
        doIteration();
        sendIterationToCollector();
        sendIterationToNeighbors();
    }

    protected void sendIterationToCollector()
    {
        logger.debug(String.format("%s sends its iteration to the data collector", this.agent.getAgentData().getName()));

        AID foundAgentAID = null;
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(DataCollectionCommunicator.SERVICE_TYPE);

        template.addServices(sd);

        try {
            DFAgentDescription[] result = DFService.search(this.agent, template);
            if (result.length > 0)
            {
                logger.debug(String.format("found %d %s agents, this first one's AID is %s",
                        result.length,
                        DataCollectionCommunicator.SERVICE_TYPE,
                        result[0].getName().toString()));
                ACLMessage message = new ACLMessage(ACLMessage.REQUEST);//TODO gal reconsider the type
                message.setOntology(agent.getProblemId()+agent.getAlgoId());
                for (DFAgentDescription foundAID : result)
                {
                    message.addReceiver(foundAID.getName());
                }
                message.setContentObject(agentIteraionCollected);
                agent.send(message);
            }
            else
            {
                logger.error("could not find the data communicator");//TODO gal decide how to handle such scenario);
            }
        }
        catch (FIPAException | IOException | NullPointerException e) {
            logger.error(e);
        }

    }

    protected void sendIterationToNeighbors()
    {
        logger.debug(String.format("%s sends its iteration to its neighbours", this.agent.getAgentData().getName()));

        ACLMessage aclmsg = new ACLMessage(ACLMessage.REQUEST);
        agent.getAgentData().getNeighbors().stream()
                .map(neighbor -> new AID(neighbor.getName(), AID.ISLOCALNAME))
                .forEach(aclmsg::addReceiver);

        try {
            aclmsg.setContentObject(agent.getCurrIteration());
            agent.send(aclmsg);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    public abstract SmartHomeAgentBehaviour cloneBehaviour();

    //a blocking method that waits far receiving messages(without filtration) from all neighbours and data collector
    protected List<ACLMessage> waitForNeighbourMessages() {
        List<ACLMessage> messages = new ArrayList<>();
        ACLMessage receivedMessage;
        int neighbourCount = this.agent.getAgentData().getNeighbors().size();
        //TODO wait also for DATA COLLECTOR Message
        while (messages.size() < neighbourCount)//the additional one is for the data collector's message
//        while (messages.size() < neighbourCount)
        {
            receivedMessage = this.agent.blockingReceive(MessageTemplate.not(SmartHomeAgent.MESSAGE_TEMPLATE_SENDER_IS_COLLERCTOR));
            logger.debug(Utils.parseAgentName(this.agent) + " received a message from " + Utils.parseAgentName(receivedMessage.getSender()));
            messages.add(receivedMessage);
        }
        // wait for the message from the collector
        receivedMessage = this.agent.blockingReceive(SmartHomeAgent.MESSAGE_TEMPLATE_SENDER_IS_COLLERCTOR);
        logger.debug(Utils.parseAgentName(this.agent) + " received a message from " + Utils.parseAgentName(receivedMessage.getSender()));
        try {
            this.agent.setcSum(Double.parseDouble(receivedMessage.getContent()));
        }catch(Exception e){
            logger.error("could not parse cSum sent from the data collector", e);
        }
        return messages;
    }

    protected void parseMessages(List<ACLMessage> messageList) {
        //TODO: Recognized Aviv message.
        List<AgentIterationData> neighbors = new ArrayList<>();
        for (int i=0; i< messageList.size(); ++i)
        {
            try {
                neighbors.add((AgentIterationData)messageList.get(i).getContentObject());
            } catch (UnreadableException e) {
                e.printStackTrace();
            }
        }

        agent.setMyNeighborsShed(neighbors);
    }

    protected double calcPrice(double[] powerConsumption) {
        double res = 0 ;
        double [] priceScheme = agent.getAgentData().getPriceScheme();
        for (int i=0 ; i<priceScheme.length; ++i)
        {
            double temp =  Double.sum(powerConsumption[i], priceScheme[i]);
            res = Double.sum(temp, res);
        }
        return res;
    }

    // used by the agent instance to complete the initialization of the behaviour
    protected void initializeBehaviourWithAgent(SmartHomeAgent agent)
    {
        this.agent = agent;
        this.currentNumberOfIter =0;
        this.FINAL_TICK = agent.getAgentData().getBackgroundLoad().length -1;
        this.helper = new AlgorithmDataHelper(agent);
    }

    protected void addBackgroundLoadToPriceScheme(double[] powerConsumption)
    {
        double [] backgroundLoad = agent.getAgentData().getBackgroundLoad();
        double [] newPowerCons = new double[backgroundLoad.length];
        for (int i=0 ; i<backgroundLoad.length; ++i)
        {
            newPowerCons[i] =  Double.sum(powerConsumption[i], backgroundLoad[i]);
        }
        helper.setPowerConsumption(newPowerCons);
    }

    protected void calcBestIteration ()
    {

    }
}
