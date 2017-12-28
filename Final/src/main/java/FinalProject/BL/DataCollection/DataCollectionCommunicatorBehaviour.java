package FinalProject.BL.DataCollection;

import FinalProject.BL.IterationData.IterationCollectedData;
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
        if (msg != null ) {
            String senderName = msg.getSender().getName();
            if (!senderName.startsWith("ams")) {
                logger.info("received a message from: " + msg.getSender().getName());
                // Message received. Process it
                try {
                    IterationCollectedData ICD = (IterationCollectedData) msg.getContentObject();
                    cSumReturned = agent.getCollector().addData(ICD);
                    if (cSumReturned != 0) { //iteration finished
                        logger.info("iteration number " + ICD.getIterNum() + " finished.");
                        sendCsumToEveryone(msg, cSumReturned);
                        if (ICD.getIterNum() == iterationNum) { //last iteration finished (algo&prob finished)
                            logger.info("Algo: " + ICD.getAlgorithm() + " Problem: " + ICD.getProblemId() + " finished.");
                            agent.getExperiment().algorithmRunEnded(
                                    agent.getCollector().getAlgoProblemResult(ICD.getProblemId(), ICD.getAlgorithm()));
                        }
                    }
                } catch (UnreadableException e) {
                    logger.error(e);
                } catch (ClassCastException e) {
                    logger.error(e);
                }
            }
        }else{
            block();
        }
    }

    private void sendCsumToEveryone(ACLMessage msg, double cSumReturned) {
        DFAgentDescription[] agents = findAgents(msg.getOntology());
        ACLMessage replay;
        for (DFAgentDescription dfa: agents) {
            replay = new ACLMessage(ACLMessage.INFORM);
            replay.addReceiver(dfa.getName());
            replay.setContent(String.valueOf(cSumReturned));
            agent.send(replay);
        }
    }

    public DFAgentDescription[] findAgents(String onotology)
    {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.addOntologies(onotology);
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
