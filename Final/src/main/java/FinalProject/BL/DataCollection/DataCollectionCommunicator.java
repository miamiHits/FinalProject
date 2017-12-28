package FinalProject.BL.DataCollection;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import FinalProject.BL.Agents.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.apache.log4j.Logger;

import java.util.Map;

public class DataCollectionCommunicator extends Agent {

    public static final String SERVICE_TYPE = "dataCollector";
    public static final String SERVICE_NAME = "DataCollectionCommunicator";

    private static final Logger logger = Logger.getLogger(DataCollectionCommunicator.class);

    DataCollector collector;

    public DataCollectionCommunicator() {
    }

    public DataCollectionCommunicator(Map<String, Integer> numOfAgentsInProblems, Map<String, double[]> prices) {
        collector = new DataCollector(numOfAgentsInProblems, prices);
    }

    @Override
    protected void setup() {
        logger.info("starting communicator");
        //add the cyclic behaviour
        addBehaviour(new DataCollectionCommunicatorBehaviour());

        // Register the book-selling service in the yellow pages
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(SERVICE_TYPE);
        sd.setName(SERVICE_NAME);
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
