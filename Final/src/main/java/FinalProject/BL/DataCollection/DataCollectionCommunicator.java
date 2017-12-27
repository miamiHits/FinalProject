package FinalProject.BL.DataCollection;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Map;

public class DataCollectionCommunicator extends Agent {
    DataCollector collector;

    public DataCollectionCommunicator(Map<String, Integer> numOfAgentsInProblems, Map<String, double[]> prices) {
        collector = new DataCollector(numOfAgentsInProblems, prices);
    }

    @Override
    protected void setup() {
        //add the cyclic behaviour
        addBehaviour(new DataCollectionCommunicatorBehaviour());

        // Register the book-selling service in the yellow pages
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("dataCollector");
        sd.setName("DataCollectionCommunicator");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
        System.out.println("Agent " + getLocalName() + ": waiting for REQUEST message...");

        /*ACLMessage msg = blockingReceive(MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
        System.out.println("Agent " + getLocalName() + ": REQUEST message received. Reply and exit.");
        ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
        reply.addReceiver(msg.getSender());
        msg.setOntology("XXXXX");
        reply.setContent("exiting");
        send(reply);
        doDelete();*/
    }

    protected void takeDown() {
        // Deregister from the yellow pages
        try {
            DFService.deregister(this);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
        // Printout a dismissal message
        System.out.println("DataCollectionCommunicator "+getAID().getName()+" terminating.");
    }

}
