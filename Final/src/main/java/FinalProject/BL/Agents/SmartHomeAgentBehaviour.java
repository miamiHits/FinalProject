package FinalProject.BL.Agents;


import FinalProject.DAL.AlgorithmLoader;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import org.apache.log4j.Logger;

import java.io.IOException;

public abstract class SmartHomeAgentBehaviour extends Behaviour {

    public  String agentName;
    public SmartHomeAgent agent;
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
        ACLMessage aclmsg = new ACLMessage(ACLMessage.REQUEST);
        agent.getAgentData().getNeighbors().stream()
                .map(neighbor -> new AID(neighbor.getName(), AID.ISLOCALNAME)).forEach(aclmsg::addReceiver);

        try {
            aclmsg.setContentObject(agent.getCurrIteration());
            agent.send(aclmsg);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

}
