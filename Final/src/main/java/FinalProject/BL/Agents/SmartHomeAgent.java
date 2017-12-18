package FinalProject.BL.Agents;

import FinalProject.BL.IterationData.AgentIterationData;
import FinalProject.BL.Problems.AgentData;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import java.text.SimpleDateFormat;
import java.util.Date;
public class SmartHomeAgent extends Agent {
    public static final String SERVICE_TYPE = "Algorithms";
    public static final String DSA_SERVICE_NAME = "DSAService";
    private AgentData agentData;
    private AgentIterationData bestIteration;
    private AgentIterationData currIteration;
    private AlgorithmName behaviorName;

    public enum AlgorithmName
    {
        DSA
    }
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

    public AlgorithmName getBehaviorName() {
        return behaviorName;
    }


    @Override
    protected void setup() {
        super.setup();
        //Getting fields in order: Algorithm, agentData
        this.behaviorName = (AlgorithmName) getArguments()[0];
        this.agentData = (AgentData) getArguments()[1];
        switch (this.behaviorName)
        {
            case DSA:
                int iterationTotalNumber = agentData.getIterationNums();
                for(int i=0; i<iterationTotalNumber; i++)
                {
                    createDSAAgent();
                }
                break;
            default:
                throw new NotImplementedException();
        }
    }

    private void createDSAAgent() {
        addBehaviour(new DSA(this.agentData.getName(), this));
        // register to the services.
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(SmartHomeAgent.SERVICE_TYPE);
        sd.setName(SmartHomeAgent.DSA_SERVICE_NAME);
        dfd.addServices(sd);
        try
        {
            DFService.register(this, dfd);

        }
        catch (FIPAException e)
        {
            e.printStackTrace();
        }

        this.printLog("start working on my DSA");
    }

    public void buildSchedule() {
    int[] costPerTik = new int [agentData.getBackgroundLoad().length];
    for(int i=0; i<costPerTik.length; ++i)
    {

    }

    }

    public void printLog(String message)
    {
        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
        String agentName = new String(this.getAID().getName().toString());
        agentName = agentName.substring(0, agentName.indexOf('@'));
        System.out.println(String.format("%s - %s: %S", timeStamp, agentName, message));
    }
}
