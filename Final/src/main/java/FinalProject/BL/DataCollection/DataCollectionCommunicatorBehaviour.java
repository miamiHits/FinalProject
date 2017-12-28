package FinalProject.BL.DataCollection;

import FinalProject.BL.Agents.SmartHomeAgentBehaviour;
import FinalProject.BL.IterationData.IterationCollectedData;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import org.apache.log4j.Logger;


public class DataCollectionCommunicatorBehaviour extends CyclicBehaviour {
    private DataCollectionCommunicator agent;
    private int iterationNum;
    private final static Logger logger = org.apache.log4j.Logger.getLogger(DataCollectionCommunicatorBehaviour.class);

    @Override
    public void action() {
        agent = ((DataCollectionCommunicator)myAgent);
        iterationNum = agent.getExperiment().maximumIterations;
        double cSumReturned;

        ACLMessage msg = myAgent.receive();
        if (msg != null) {
            logger.debug("received a message from: " + msg.getSender().getName());
            // Message received. Process it
            String title = msg.getContent();
            ACLMessage reply = msg.createReply();
            try{
               IterationCollectedData ICD = (IterationCollectedData)msg.getContentObject();
                cSumReturned = agent.getCollector().addData(ICD);
                if (cSumReturned != 0){ //iteration finished
                    sendCsumToEveryone(cSumReturned);
                    if (ICD.getIterNum() == iterationNum){ //last iteration finished (algo&prob finished)
                        agent.getExperiment().algorithmRunEnded(ICD.getProblemId(), ICD.getAlgorithm());
                    }
                }
            }catch(UnreadableException e){
                //todo
            }
            catch(ClassCastException e)
            {
                //TODO gal got this exception, probably because action was not fully implemented
                logger.error(e);
            }

        //reply.setPerformative(ACLMessage.PROPOSE);
            //reply.setContent(String.valueOf(price.intValue()));
            myAgent.send(reply);
        }else{
            block();
        }
    }
}
