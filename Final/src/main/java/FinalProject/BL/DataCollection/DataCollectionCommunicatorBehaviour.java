package FinalProject.BL.DataCollection;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class DataCollectionCommunicatorBehaviour extends CyclicBehaviour {

    @Override
    public void action() {
        ACLMessage msg = myAgent.receive();
        if (msg != null) {
            // Message received. Process it
            String title = msg.getContent();
            ACLMessage reply = msg.createReply();
            //reply.setPerformative(ACLMessage.PROPOSE);
            //reply.setContent(String.valueOf(price.intValue()));
            myAgent.send(reply);
        }else{
            block();
        }
    }
}
