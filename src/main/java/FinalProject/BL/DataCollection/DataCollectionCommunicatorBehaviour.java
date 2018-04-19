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

import java.util.HashMap;
import java.util.Map;


public class DataCollectionCommunicatorBehaviour extends CyclicBehaviour {
    private DataCollectionCommunicator agent;
    private int iterationNum;
    private final static Logger logger = org.apache.log4j.Logger.getLogger(DataCollectionCommunicatorBehaviour.class);

    @Override
    public void action() {
        agent = ((DataCollectionCommunicator)myAgent);
        iterationNum = agent.getExperiment().maximumIterations;
        Double cSumReturned;

        ACLMessage msg = myAgent.receive();
        if (msg != null ) {
            String senderName = msg.getSender().getName();
            if (!senderName.startsWith("ams")) {
                logger.info("received a message from: " + msg.getSender().getLocalName());
                // Message received. Process it
                try {
                    IterationCollectedData ICD = (IterationCollectedData) msg.getContentObject();
                    cSumReturned = agent.getCollector().addData(ICD);
                    if(cSumReturned == -1.0){ //iteration finished
                        if (ICD.getIterNum() == iterationNum ) { //last iteration finished (algo&prob finished)
                            logger.info("Algo: " + ICD.getAlgorithm() + " Problem: " + ICD.getProblemId() + " finished.");
                            calcBestPricePerIterationIfNecessary(ICD);
                            agent.getExperiment().algorithmProblemComboRunEnded(
                                    agent.getCollector().getAlgoProblemResult(ICD.getProblemId(), ICD.getAlgorithm()));
                        }
                        else {
                            agent.getExperiment().algorithmProblemIterEnded(ICD.getAlgorithm(), ICD.getProblemId());
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

    private void calcBestPricePerIterationIfNecessary(IterationCollectedData icd) {
        if (icd.getAlgorithm() != "SHMGM"){return;}
        AlgorithmProblemResult pr = agent.getCollector().getAlgoProblemResult
                (icd.getProblemId(), icd.getAlgorithm());
        Map<Integer, Double> results = pr.getTotalGradePerIteration();
        Map<Integer, Double> bestResults = new HashMap<Integer, Double>();
        Double best = results.get(0);
        bestResults.put(0,best);
        int i = 1;
        double res;
        while (results.get(i) != null){
            res = results.get(i);
            if (res < best){best = res;}
            bestResults.put(i, best);
            i++;
        }
        pr.setBestTotalGradePerIter(bestResults);

    }

    private void sendCsumToEveryone(ACLMessage msg, double cSumReturned) {
        logger.info("sending c_sum to all agents in the experiment");
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
