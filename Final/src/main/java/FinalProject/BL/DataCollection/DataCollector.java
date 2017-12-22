package FinalProject.BL.DataCollection;

import java.util.Map;

public class DataCollector {

    private Map<String, Integer> numOfAgentsInProblem;
    private Map<ProblemAlgorithm, Map<Integer, AgentPrice>> collection;

    public DataCollector(Map<String, Integer> numOfAgentsInProblem) {
        this.numOfAgentsInProblem = numOfAgentsInProblem;
    }

    public Map<String, Integer> getNumOfAgentsInProblem() {
        return numOfAgentsInProblem;
    }

    public void setNumOfAgentsInProblem(Map<String, Integer> numOfAgentsInProblem) {
        this.numOfAgentsInProblem = numOfAgentsInProblem;
    }



}
