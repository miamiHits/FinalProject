package FinalProject.BL.DataCollection;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class DataCollectionCommunicator extends Agent {

    public DataCollectionCommunicator() {
    }

    @Override
    protected void setup() {
        while (true) {
            System.out.println("Agent " + getLocalName() + ": waiting for REQUEST message...");
            ACLMessage msg = blockingReceive(MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
            System.out.println("Agent " + getLocalName() + ": REQUEST message received. Reply and exit.");
            ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
            reply.addReceiver(msg.getSender());
            reply.setContent("exiting");
            send(reply);
            doDelete();
        }
    }
}
