package FinalProject.BL.Agents;

import FinalProject.BL.IterationData.AgentIterationData;
import FinalProject.BL.Problems.AgentData;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

import java.io.Serializable;
import java.util.*;

public class SmartHomeAgent extends Agent {
    public static final String SERVICE_TYPE = "Algorithms";
    private AgentData agentData;
    private AgentIterationData bestIteration;
    private AgentIterationData currIteration;
    private SmartHomeAgentBehaviour algorithm;
    private boolean isZEROIteration;
    private int IterationNum = 0;
    private List<Serializable> myNeighborsShed = new ArrayList<>();
    private boolean stop = false;

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

    public List<Serializable> getMyNeighborsShed() {
        return myNeighborsShed;
    }

    public void setMyNeighborsShed(List<Serializable> myNeighborsShed) {
        this.myNeighborsShed = myNeighborsShed;
    }

    public int getIterationNum() {
        return IterationNum;
    }

    public boolean getStop() {return this.stop;}

    public void setStop(boolean stop) {
        this.stop = stop;
    }
    @Override
    protected void setup() {
        super.setup();
        //Getting fields in order: Algorithm, agentData
        this.algorithm = (SmartHomeAgentBehaviour) getArguments()[0];
        this.agentData = (AgentData) getArguments()[1];
        this.isZEROIteration = true;

        int iterationTotalNumber = agentData.getNumOfIterations();
        for(int i=0; i<iterationTotalNumber; i++)
        {
            createAlgorithmAgent();
            this.IterationNum++;
        }

    }

    private void createAlgorithmAgent() {
        addBehaviour(this.algorithm);
        // register to the services.
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(SmartHomeAgent.SERVICE_TYPE);
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
