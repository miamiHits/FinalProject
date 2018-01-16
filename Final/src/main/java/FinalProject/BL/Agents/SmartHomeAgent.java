package FinalProject.BL.Agents;

import FinalProject.BL.DataCollection.DataCollectionCommunicator;
import FinalProject.BL.IterationData.AgentIterationData;
import FinalProject.BL.DataObjects.AgentData;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.MessageTemplate;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SmartHomeAgent extends Agent implements Serializable{
    public static final String SERVICE_TYPE = "ACCESS_FOR_ALL_AGENTS";
    public static final String SERVICE_NAME = "AGENT";//TODO gal consider this one to be the agent's name(not static)
    public static MessageTemplate MESSAGE_TEMPLATE_SENDER_IS_COLLECTOR = MessageTemplate.MatchSender(new AID(DataCollectionCommunicator.SERVICE_NAME, false));
    public static MessageTemplate MESSAGE_TEMPLATE_SENDER_IS_AMS = MessageTemplate.MatchSender(new AID("ams", false));
    public static MessageTemplate MESSAGE_TEMPLATE_SENDER_IS_NEIGHBOUR =
            MessageTemplate.and(
                    MessageTemplate.not(MESSAGE_TEMPLATE_SENDER_IS_COLLECTOR),
                    MessageTemplate.not(MESSAGE_TEMPLATE_SENDER_IS_AMS));

    private AgentData agentData;
    private AgentIterationData bestIteration;
    private AgentIterationData currIteration;
    private SmartHomeAgentBehaviour algorithm;
    private boolean isZEROIteration;
    private List<AgentIterationData> myNeighborsShed = new ArrayList<>();
    private boolean stop = false;
    private double cSum;
    private String problemId;
    private String algoId;

    private Logger logger = Logger.getLogger(SmartHomeAgent.class);

    public AgentData getAgentData() {
        return agentData;
    }

    public void setAgentData(AgentData agentData) {
        this.agentData = agentData;
    }

    public AgentIterationData getBestIteration() {
        return bestIteration;
    }

    public void setBestIteration(AgentIterationData bestIteration) {
        this.bestIteration = bestIteration;
    }

    public AgentIterationData getCurrIteration() {
        return currIteration;
    }

    public void setCurrIteration(AgentIterationData currIteration) {
        this.currIteration = currIteration;
    }

    public boolean isZEROIteration() {
        return isZEROIteration;
    }

    public void setZEROIteration(boolean ZEROIteration) {
        isZEROIteration = ZEROIteration;
    }

    public List<AgentIterationData> getMyNeighborsShed() {
        return myNeighborsShed;
    }

    public void setMyNeighborsShed(List<AgentIterationData> myNeighborsShed) {
        this.myNeighborsShed = myNeighborsShed;
    }

    public boolean getStop() {return this.stop;}

    public double getcSum() {
        return cSum;
    }

    public void setcSum(double cSum) {
        this.cSum = cSum;
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }

    public String getProblemId() {
        return problemId;
    }

    public void setProblemId(String problemId) {
        this.problemId = problemId;
    }

    public String getAlgoId() {
        return algoId;
    }

    public void setAlgoId(String algoId) {
        this.algoId = algoId;
    }
    @Override
    protected void setup() {
        super.setup();
        //Getting fields in order: Algorithm, agentData
        this.algorithm = (SmartHomeAgentBehaviour) getArguments()[0];
        this.agentData = (AgentData) getArguments()[1];
        this.algoId = (String)getArguments()[2];
        this.problemId = (String)getArguments()[3];
        this.isZEROIteration = true;
        createAlgorithmAgent();
    }

    @Override
    protected void takeDown()
    {
        // Deregister from the yellow pages
        try {
            DFService.deregister(this);
        }
        catch (FIPAException fe) {
            logger.error("failed to terminate: " + fe);
        }
        // Printout a dismissal message
        logger.info("agent " + getAID().getName() + " terminating.");
    }



    private void createAlgorithmAgent() {
        this.algorithm.initializeBehaviourWithAgent(this);
        addBehaviour(this.algorithm);
        // register to the services.
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(SmartHomeAgent.SERVICE_TYPE);
        sd.setName(SmartHomeAgent.SERVICE_NAME);
        sd.addOntologies(problemId+algoId);
        dfd.addServices(sd);
        try
        {
            DFService.register(this, dfd);

        }
        catch (FIPAException e)
        {
            e.printStackTrace();
        }

    }
}
