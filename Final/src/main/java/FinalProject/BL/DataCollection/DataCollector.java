package FinalProject.BL.DataCollection;

import FinalProject.BL.IterationData.IterationCollectedData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataCollector {

    private Map<String, Integer> numOfAgentsInProblem;
    private Map<ProblemAlgorithm, IterationAgentsPrice> collection;

    public DataCollector(Map<String, Integer> numOfAgentsInProblem) {
        this.numOfAgentsInProblem = numOfAgentsInProblem;
        this.collection = new HashMap<ProblemAlgorithm, IterationAgentsPrice>();
    }

    public void AddData (IterationCollectedData data){
        ProblemAlgorithm tempPA = new ProblemAlgorithm(data.getProblemId(), data.getAlgorithm());
        if (collection.containsKey(tempPA)){
            collection.get(tempPA);//todo
        }
    }




    public Map<String, Integer> getNumOfAgentsInProblem() {
        return numOfAgentsInProblem;
    }

    public void setNumOfAgentsInProblem(Map<String, Integer> numOfAgentsInProblem) {
        this.numOfAgentsInProblem = numOfAgentsInProblem;
    }



}
