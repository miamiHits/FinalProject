package FinalProject.BL.DataCollection;

import FinalProject.BL.Agents.SmartHomeAgentBehaviour;
import FinalProject.BL.IterationData.IterationCollectedData;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
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

    private void sendCsumToEveryone(double cSumReturned) {

    }

    public DFAgentDescription[] findAgents()
    {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType("ACCESS_FOR_ALL_AGENTS");
        template.addServices(sd);
        try {
            return DFService.search(agent, template);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
            return null;
        }
    }
}
