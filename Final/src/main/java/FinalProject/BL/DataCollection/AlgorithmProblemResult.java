package FinalProject.BL.DataCollection;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class AlgorithmProblemResult {
    private String problem;
    private String algorithm;
    private Map<Integer, Double> avgPricePerIteration;
    private Map<Integer, Double> totalGradePerIteration;
    private int iterationsTillBestPrice;
    private double bestPrice;
    private double lowestCostForAgentInBestIteration;
    private String lowestCostForAgentInBestIterationAgentName;
    private double highestCostForAgentInBestIteration;
    private String highestCostForAgentInBestIterationAgentName;
    private static final Logger logger = Logger.getLogger(AlgorithmProblemResult.class);

    public AlgorithmProblemResult(ProblemAlgorithm probAlgo) {
        problem = probAlgo.getProblemId();
        algorithm = probAlgo.getAlgorithmName();
        avgPricePerIteration = new HashMap<Integer, Double>();
        totalGradePerIteration = new HashMap<Integer, Double>();
        iterationsTillBestPrice = 0;
        bestPrice = Double.MAX_VALUE;
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

    public double getLowestCostForAgentInBestIteration() {
        return lowestCostForAgentInBestIteration;
    }

    public void setLowestCostForAgentInBestIteration(double lowestCostForAgentInBestIteration) {
        this.lowestCostForAgentInBestIteration = lowestCostForAgentInBestIteration;
    }

    public String getLowestCostForAgentInBestIterationAgentName() {
        return lowestCostForAgentInBestIterationAgentName;
    }

    public void setLowestCostForAgentInBestIterationAgentName(String lowestCostForAgentInBestIterationAgentName) {
        this.lowestCostForAgentInBestIterationAgentName = lowestCostForAgentInBestIterationAgentName;
    }

    public double getHighestCostForAgentInBestIteration() {
        return highestCostForAgentInBestIteration;
    }

    public void setHighestCostForAgentInBestIteration(double highestCostForAgentInBestIteration) {
        this.highestCostForAgentInBestIteration = highestCostForAgentInBestIteration;
    }

    public String getHighestCostForAgentInBestIterationAgentName() {
        return highestCostForAgentInBestIterationAgentName;
    }

    public void setHighestCostForAgentInBestIterationAgentName(String highestCostForAgentInBestIterationAgentName) {
        this.highestCostForAgentInBestIterationAgentName = highestCostForAgentInBestIterationAgentName;
    }

    public double getBestPrice() {
        return bestPrice;
    }

    public void setBestPrice(double bestPrice) {
        this.bestPrice = bestPrice;
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

    @Override
    public String toString()
    {
        return "AlgorithmProblemResult{" +
                "problem='" + problem + '\'' + "\n" +
                ", algorithm='" + algorithm + '\'' + "\n" +
                ", avgPricePerIteration=" + avgPricePerIteration + "\n" +
                ", totalGradePerIteration=" + totalGradePerIteration + "\n" +
                ", iterationsTillBestPrice=" + iterationsTillBestPrice + "\n" +
                ", bestPrice=" + bestPrice + "\n" +
                ", lowestCostForAgentInBestIteration=" + lowestCostForAgentInBestIteration + "\n" +
                ", lowestCostForAgentInBestIterationAgentName='" + lowestCostForAgentInBestIterationAgentName + '\'' + "\n" +
                ", highestCostForAgentInBestIteration=" + highestCostForAgentInBestIteration + "\n" +
                ", highestCostForAgentInBestIterationAgentName='" + highestCostForAgentInBestIterationAgentName + '\'' +"\n" +
                '}';
    }

}