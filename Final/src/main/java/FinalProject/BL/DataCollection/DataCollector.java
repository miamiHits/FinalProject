package FinalProject.BL.DataCollection;

import FinalProject.BL.IterationData.IterationCollectedData;

import java.util.HashMap;
import java.util.Map;

public class DataCollector {

    private Map<String, Integer> numOfAgentsInProblems;
    private Map<ProblemAlgorithm, IterationAgentsPrice> probAlgoToItAgentPrice;

    public DataCollector(Map<String, Integer> numOfAgentsInProblems) {
        this.numOfAgentsInProblems = numOfAgentsInProblems;
        this.probAlgoToItAgentPrice = new HashMap<ProblemAlgorithm, IterationAgentsPrice>();
    }

    public void addData (IterationCollectedData data){
        ProblemAlgorithm tempPA = new ProblemAlgorithm(data.getProblemId(), data.getAlgorithm());
        IterationAgentsPrice tempIAP;
        if (probAlgoToItAgentPrice.containsKey(tempPA)){
            tempIAP = probAlgoToItAgentPrice.get(tempPA);
            tempIAP.addAgentPrice(data.getIterNum(),
                    new AgentPrice(data.getAgentName(), data.getPrice()));
        }else{
            tempIAP = new IterationAgentsPrice();
            tempIAP.addAgentPrice(data.getIterNum(),
                    new AgentPrice(data.getAgentName(), data.getPrice()));
            probAlgoToItAgentPrice.put(tempPA, tempIAP);
        }
    }

    public int getNumOfAgentsInProblem(String problemName){
        if (numOfAgentsInProblems.containsKey(problemName)){
            return numOfAgentsInProblems.get(problemName);
        }
        return 0;
    }

    public Map<String, Integer> getNumOfAgentsInProblems() {
        return numOfAgentsInProblems;
    }

    public void setNumOfAgentsInProblems(Map<String, Integer> numOfAgentsInProblems) {
        this.numOfAgentsInProblems = numOfAgentsInProblems;
    }

    public Map<ProblemAlgorithm, IterationAgentsPrice> getProbAlgoToItAgentPrice() {
        return probAlgoToItAgentPrice;
    }

    public void setProbAlgoToItAgentPrice(Map<ProblemAlgorithm, IterationAgentsPrice> probAlgoToItAgentPrice) {
        this.probAlgoToItAgentPrice = probAlgoToItAgentPrice;
    }


}
