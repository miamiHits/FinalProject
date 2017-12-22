package FinalProject.BL.DataCollection;

import FinalProject.BL.IterationData.IterationCollectedData;

import java.util.HashMap;
import java.util.Map;

public class DataCollector {

    private Map<String, Integer> numOfAgentsInProblem;
    private Map<ProblemAlgorithm, IterationAgentsPrice> collection;

    public DataCollector(Map<String, Integer> numOfAgentsInProblem) {
        this.numOfAgentsInProblem = numOfAgentsInProblem;
        this.collection = new HashMap<ProblemAlgorithm, IterationAgentsPrice>();
    }

    public void addData (IterationCollectedData data){
        ProblemAlgorithm tempPA = new ProblemAlgorithm(data.getProblemId(), data.getAlgorithm());
        IterationAgentsPrice tempIAP;
        if (collection.containsKey(tempPA)){
            tempIAP = collection.get(tempPA);
            tempIAP.addAgentPrice(data.getIterNum(),
                    new AgentPrice(data.getAgentName(), data.getPrice()));
        }else{
            tempIAP = new IterationAgentsPrice();
            tempIAP.addAgentPrice(data.getIterNum(),
                    new AgentPrice(data.getAgentName(), data.getPrice()));
            collection.put(tempPA, tempIAP);
        }
    }

    public Map<String, Integer> getNumOfAgentsInProblem() {
        return numOfAgentsInProblem;
    }

    public void setNumOfAgentsInProblem(Map<String, Integer> numOfAgentsInProblem) {
        this.numOfAgentsInProblem = numOfAgentsInProblem;
    }



}
