package FinalProject.BL.Agents;


import FinalProject.BL.IterationData.AgentIterationData;
import FinalProject.DAL.AlgorithmLoader;
import FinalProject.Utils;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class SmartHomeAgentBehaviour extends Behaviour {

    public  String agentName;
    public SmartHomeAgent agent;
    protected int currentNumberOfIter;
    protected int FINAL_TICK;
    protected AlgorithmDataHelper helper;

    private final static Logger logger = Logger.getLogger(SmartHomeAgentBehaviour.class);

    protected abstract void doIteration();

    @Override
    public void action() {
        while (!agent.getStop())
        {
            logger.info("Starting work on Iteration: " + agent.getIterationNum());
            doIteration();
            sendIterationToCollector();
            sendIterationToNeighbors();
        }
    }

    protected void sendIterationToCollector()
    {
        //TODO: Send
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
//        while (messages.size() < neighbourCount + 1)//the additional one is for the data collector's message
        while (messages.size() < neighbourCount)
        {
            receivedMessage = this.agent.blockingReceive();
            logger.debug(Utils.parseAgentName(this.agent) + " received a message from " + Utils.parseAgentName(receivedMessage.getSender()));
            messages.add(receivedMessage);
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
        double [] backgroundLoad = agent.getAgentData().getBackgroundLoad();
        double [] priceScheme = agent.getAgentData().getPriceScheme();
        for (int i=0 ; i<backgroundLoad.length; ++i)
        {
            double temp =  Double.sum(powerConsumption[i], priceScheme[i]);
            double temp2 = Double.sum(temp,backgroundLoad[i] );
            res = Double.sum(temp2, res);
            // Double.sum(res, Double.sum(backgroundLoad[i], Double.sum(powerConsumption[i], priceScheme[i])));
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
}
