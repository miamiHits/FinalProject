package FinalProject.BL.DataCollection;

import FinalProject.BL.IterationData.IterationCollectedData;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

public class DataCollectionCommunicatorBehaviour extends CyclicBehaviour {
    private DataCollectionCommunicator agent;

    @Override
    public void action() {
        ACLMessage msg = myAgent.receive();
        if (msg != null) {
            // Message received. Process it
            String title = msg.getContent();
            ACLMessage reply = msg.createReply();
            try{
               IterationCollectedData ICD = (IterationCollectedData)msg.getContentObject();
               agent.collector.addData(ICD);
            }catch(UnreadableException e){
                //todo
            }

        //reply.setPerformative(ACLMessage.PROPOSE);
            //reply.setContent(String.valueOf(price.intValue()));
            myAgent.send(reply);
        }else{
            block();
        }
    }
}
