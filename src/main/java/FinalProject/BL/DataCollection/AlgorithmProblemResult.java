package FinalProject.BL.DataCollection;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlgorithmProblemResult {
    private String problem;
    private String algorithm;
    private Map<Integer, Double> avgPricePerIteration;
    private Map<Integer, Double> totalGradePerIteration;
    private int iterationsTillBestPrice;
    private double bestGrade;
    private Map<Integer, Double> lowestCostForAgentInBestIteration;
    private Map<Integer, String> lowestCostForAgentInBestIterationAgentName;
    private Map<Integer, Double> highestCostForAgentInBestIteration;
    private Map<Integer, String> highestCostForAgentInBestIterationAgentName;
    private static final Logger logger = Logger.getLogger(AlgorithmProblemResult.class);
    private Map<Integer, MsgInfo> totalMessagesInIter;

    public AlgorithmProblemResult(ProblemAlgorithm probAlgo) {
        problem = probAlgo.getProblemId();
        algorithm = probAlgo.getAlgorithmName();
        avgPricePerIteration = new HashMap<Integer, Double>();
        totalGradePerIteration = new HashMap<Integer, Double>();
        totalMessagesInIter = new HashMap<Integer, MsgInfo>();
        lowestCostForAgentInBestIteration = new  HashMap<Integer, Double>();
        lowestCostForAgentInBestIterationAgentName =  new  HashMap<Integer, String>();
        highestCostForAgentInBestIteration = new  HashMap<Integer, Double>();
        highestCostForAgentInBestIterationAgentName =  new  HashMap<Integer, String>();
        iterationsTillBestPrice = 0;
        bestGrade = Double.MAX_VALUE;
    }

    public String getProblem() {
        return problem;
    }

    public void setProblem(String problem) {
        this.problem = problem;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public Map<Integer, Double> getAvgPricePerIteration() {
        return avgPricePerIteration;
    }

    public void setAvgPricePerIteration(Map<Integer, Double> avgPricePerIteration) {
        this.avgPricePerIteration = avgPricePerIteration;
    }

    public int getIterationsTillBestPrice() {
        return iterationsTillBestPrice;
    }

    public void setIterationsTillBestPrice(int iterationsTillBestPrice) {
        this.iterationsTillBestPrice = iterationsTillBestPrice;
    }

    public Map<Integer, Double> getLowestCostForAgentInBestIteration() {
        return lowestCostForAgentInBestIteration;
    }

    public void setLowestCostForAgentInBestIteration(int iterNum, double lowestCostForAgentInBestIteration) {
        this.lowestCostForAgentInBestIteration.put(iterNum, lowestCostForAgentInBestIteration);
    }

    public Map<Integer, String> getLowestCostForAgentInBestIterationAgentName() {
        return lowestCostForAgentInBestIterationAgentName;
    }

    public void setLowestCostForAgentInBestIterationAgentName(String lowestCostForAgentInBestIterationAgentName, int iterNum) {
        int shtrudel = lowestCostForAgentInBestIterationAgentName.indexOf('@');
        if (shtrudel != -1){
            lowestCostForAgentInBestIterationAgentName = lowestCostForAgentInBestIterationAgentName.substring(0, shtrudel);
        }
        this.lowestCostForAgentInBestIterationAgentName.put(iterNum, lowestCostForAgentInBestIterationAgentName);
    }

    public Map<Integer, Double> getHighestCostForAgentInBestIteration() {
        return highestCostForAgentInBestIteration;
    }

    public void setHighestCostForAgentInBestIteration(int iterNum, double highestCostForAgentInBestIteration) {
        this.highestCostForAgentInBestIteration.put(iterNum, highestCostForAgentInBestIteration);
    }

    public Map<Integer, String> getHighestCostForAgentInBestIterationAgentName() {
        return highestCostForAgentInBestIterationAgentName;
    }

    public void setHighestCostForAgentInBestIterationAgentName(String highestCostForAgentInBestIterationAgentName, int iterNum) {
        int shtrudel = highestCostForAgentInBestIterationAgentName.indexOf('@');
        if (shtrudel != -1){
            highestCostForAgentInBestIterationAgentName = highestCostForAgentInBestIterationAgentName.substring(0, shtrudel);
        }
        this.highestCostForAgentInBestIterationAgentName.put(iterNum, highestCostForAgentInBestIterationAgentName);
    }

    public double getBestGrade() {
        return bestGrade;
    }

    public void setBestGrade(double bestGrade) {
        this.bestGrade = bestGrade;
    }

    public Map<Integer, Double> getTotalGradePerIteration() {
        return totalGradePerIteration;
    }

    public void setTotalGradePerIteration(Map<Integer, Double> totalGradePerIteration) {
        this.totalGradePerIteration = totalGradePerIteration;
    }

    public void setTotalGradeToIter(int iterNum, double totalGrade) {
        Double tg = totalGradePerIteration.get(iterNum);
        if (tg != null){
            logger.warn("already had totalGrade on iter: " + iterNum);
        }
        totalGradePerIteration.put(iterNum, totalGrade);
    }

    public void setTotalMsgsInIter(int iterNum, IterationAgentsPrice iap) {
        long totalMsgSize = 0;
        int totalMsgNum = 0;
        List<AgentPrice> prices = iap.getIterationToAgentsPrice().get(iterNum);
        for (AgentPrice ap : prices){
            totalMsgNum += ap.getMsgsNum();
            totalMsgSize += ap.getMsgLength();
        }
        totalMessagesInIter.put(iterNum, new MsgInfo(totalMsgNum, totalMsgSize));
    }

    @Override
    public String toString()
    {
        return "AlgorithmProblemResult{" +
                "problem='" + problem + '\'' + "\n" +
                ", algorithm='" + algorithm + '\'' + "\n" +
                ", avgPricePerIteration=" + avgPricePerIteration + "\n" +
                ", totalGradePerIteration=" + totalGradePerIteration + "\n" +
                ", iterationsTillBestPrice=" + iterationsTillBestPrice + "\n" +
                ", bestGrade=" + bestGrade + "\n" +
                ", messagesInIter=" + totalMessagesInIter + "\n" +
                ", lowestCostForAgentInBestIteration=" + lowestCostForAgentInBestIteration.values() + "\n" +
                ", lowestCostForAgentInBestIterationAgentName='" + lowestCostForAgentInBestIterationAgentName.values() + '\'' + "\n" +
                ", highestCostForAgentInBestIteration=" + highestCostForAgentInBestIteration.values() + "\n" +
                ", highestCostForAgentInBestIterationAgentName='" + highestCostForAgentInBestIterationAgentName.values() + '\'' +"\n" +
                '}';
    }


}
