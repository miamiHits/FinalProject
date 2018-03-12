package FinalProjectTests.Experiment;

import FinalProject.BL.DataCollection.DataCollectionCommunicator;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import java.util.List;
import java.util.Set;

public class ExperimentTestUtils
{

    public static void publishTestAgentAsDataCollector(Agent a)
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

    public static void sendCSumToAllAgents(Set<String> agentNames, double cSum, Agent sendingAgent)
    {
        for (String agentName : agentNames)
        {
            ACLMessage replay = new ACLMessage(ACLMessage.INFORM);
            replay.addReceiver(new AID(agentName, false));
            replay.setContent(String.valueOf(cSum));
            sendingAgent.send(replay);
        }

    }

}
