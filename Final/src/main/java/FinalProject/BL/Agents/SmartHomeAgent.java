package FinalProject.BL.Agents;

import FinalProject.BL.DataCollection.DataCollectionCommunicator;
import FinalProject.BL.IterationData.AgentIterationData;
import FinalProject.BL.Problems.AgentData;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.MessageTemplate;

import java.io.Serializable;
import java.util.*;

public class SmartHomeAgent extends Agent {
    public static final String SERVICE_TYPE = "ACCESS_FOR_ALL_AGENTS";
    public static final String SERVICE_NAME = "AGENT";//TODO gal consider this one to be the agent's name(not static)
    public  static final MessageTemplate MESSAGE_TEMPLATE_SENDER_IS_COLLERCTOR = MessageTemplate.MatchSender(new AID(DataCollectionCommunicator.SERVICE_NAME, false));
    private AgentData agentData;
    private AgentIterationData bestIteration;
    private AgentIterationData currIteration;
    private SmartHomeAgentBehaviour algorithm;
    private boolean isZEROIteration;
    private int IterationNum = 0;
    private List<AgentIterationData> myNeighborsShed = new ArrayList<>();
    private boolean stop = false;
    private double cSum;
    private double totalHousesPrice;
    private String problemId;

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

    private String algoId;


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

    public int getIterationNum() {
        return IterationNum;
    }

    public boolean getStop() {return this.stop;}

    public double getcSum() {
        return cSum;
    }

    public void setcSum(double cSum) {
        this.cSum = cSum;
    }

    public double getTotalHousesPrice() {
        return totalHousesPrice;
    }

    public void setTotalHousesPrice(double totalHousesPrice) {
        this.totalHousesPrice = totalHousesPrice;
    }

    public void setStop(boolean stop) {
        this.stop = stop;
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

//        int iterationTotalNumber = agentData.getNumOfIterations();
//        for(int i=0; i<iterationTotalNumber; i++)
//        {
            createAlgorithmAgent();
//            this.IterationNum++;
//        }

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
