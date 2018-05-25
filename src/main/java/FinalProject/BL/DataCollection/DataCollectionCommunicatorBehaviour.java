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

import java.util.Arrays;
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
                    if (ICD.getAgentName().startsWith("h1")){
                        logger.info("H1, ITER: " + ICD.getIterNum() + " powerCons: " + Arrays.toString(ICD.getPowerConsumptionPerTick()));
                        logger.info("H1, ITER: " + ICD.getIterNum() + " powerCons sum: " + Arrays.stream(ICD.getPowerConsumptionPerTick()).sum());
                    }
                    cSumReturned = agent.getCollector().addData(ICD);
                    if(cSumReturned == -1.0){ //iteration finished
                        if (ICD.getIterNum() == iterationNum ) { //last iteration finished (algo&prob finished)
                            logger.info("Algo: " + ICD.getAlgorithm() + " Problem: " + ICD.getProblemId() + " finished.");
                            calcBestPricePerIteration(ICD);
                            agent.getExperiment().algorithmProblemComboRunEnded(
                                    agent.getCollector().getAlgoProblemResult(ICD.getProblemId(), ICD.getAlgorithm()));
                        }
                        else {
                            new Thread(() ->
                                    agent.getExperiment().algorithmProblemIterEnded(ICD.getAlgorithm(), ICD.getProblemId()))
                                    .start();
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

    private void calcBestPricePerIteration(IterationCollectedData icd) {
        try {
            AlgorithmProblemResult pr = agent.getCollector().getAlgoProblemResult
                    (icd.getProblemId(), icd.getAlgorithm());
            Map<Integer, Double> results = pr.getTotalGradePerIteration();
            Map<Integer, Double> bestResults = new HashMap<Integer, Double>();
            Double best = results.get(0);
            bestResults.put(0, best);
            int i = 1;
            double res;
            while (results.get(i) != null) {
                res = results.get(i);
                if (res < best) {
                    best = res;
                }
                bestResults.put(i, best);
                i++;
            }
            pr.setBestTotalGradePerIter(bestResults);
        }catch(Exception e){
            logger.error("exception in communicator, ", e);
        }
    }

}
