package FinalProject.BL.DataCollection;

import FinalProject.BL.IterationData.IterationCollectedData;

import java.util.HashMap;
import java.util.Map;

public class DataCollector {

    private Map<String, Integer> numOfAgentsInProblem;
    private Map<ProblemAlgorithm, Map<Integer, AgentPrice>> collection;

    public DataCollector(Map<String, Integer> numOfAgentsInProblem) {
        this.numOfAgentsInProblem = numOfAgentsInProblem;
        collection = new HashMap<ProblemAlgorithm, Map<Integer, AgentPrice>>();
    }


    public void AddData (IterationCollectedData data){
        ProblemAlgorithm tempPA = new ProblemAlgorithm(data.getProblemId(), data.getAlgorithm());
        if (collection.containsKey())
    }




    public Map<String, Integer> getNumOfAgentsInProblem() {
        return numOfAgentsInProblem;
    }

    public void setNumOfAgentsInProblem(Map<String, Integer> numOfAgentsInProblem) {
        this.numOfAgentsInProblem = numOfAgentsInProblem;
    }



}
