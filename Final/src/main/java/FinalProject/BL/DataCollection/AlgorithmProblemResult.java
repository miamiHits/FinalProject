package FinalProject.BL.DataCollection;

import java.util.HashMap;
import java.util.Map;

public class AlgorithmProblemResult {
    private String problem;
    private String algorithm;
    private Map<Integer, Double> avgPricePerIteration;
    private int iterationsTillBestPrice;
    private double lowestCostInBestIteration;
    private double lowestCostForInBestIteration;
    private String lowestCostForInBestIterationAgentName;
    private double highestCostForInBestIteration;
    private String highestCostForInBestIterationAgentName;

    public AlgorithmProblemResult(ProblemAlgorithm probAlgo) {
        problem = probAlgo.getProblemId();
        algorithm = probAlgo.getAlgorithmName();
        avgPricePerIteration = new HashMap<Integer, Double>();
        iterationsTillBestPrice = 0;
        lowestCostInBestIteration = Double.MAX_VALUE;
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

    public double getLowestCostForInBestIteration() {
        return lowestCostForInBestIteration;
    }

    public void setLowestCostForInBestIteration(double lowestCostForInBestIteration) {
        this.lowestCostForInBestIteration = lowestCostForInBestIteration;
    }

    public String getLowestCostForInBestIterationAgentName() {
        return lowestCostForInBestIterationAgentName;
    }

    public void setLowestCostForInBestIterationAgentName(String lowestCostForInBestIterationAgentName) {
        this.lowestCostForInBestIterationAgentName = lowestCostForInBestIterationAgentName;
    }

    public double getHighestCostForInBestIteration() {
        return highestCostForInBestIteration;
    }

    public void setHighestCostForInBestIteration(double highestCostForInBestIteration) {
        this.highestCostForInBestIteration = highestCostForInBestIteration;
    }

    public String getHighestCostForInBestIterationAgentName() {
        return highestCostForInBestIterationAgentName;
    }

    public void setHighestCostForInBestIterationAgentName(String highestCostForInBestIterationAgentName) {
        this.highestCostForInBestIterationAgentName = highestCostForInBestIterationAgentName;
    }

    public double getLowestCostInBestIteration() {
        return lowestCostInBestIteration;
    }

    public void setLowestCostInBestIteration(double lowestCostInBestIteration) {
        this.lowestCostInBestIteration = lowestCostInBestIteration;
    }

    @Override
    public String toString()
    {
        return "AlgorithmProblemResult{" +
                "problem='" + problem + '\'' +
                ", algorithm='" + algorithm + '\'' +
                ", avgPricePerIteration=" + avgPricePerIteration +
                ", iterationsTillBestPrice=" + iterationsTillBestPrice +
                ", lowestCostInBestIteration=" + lowestCostInBestIteration +
                ", lowestCostForInBestIteration=" + lowestCostForInBestIteration +
                ", lowestCostForInBestIterationAgentName='" + lowestCostForInBestIterationAgentName + '\'' +
                ", highestCostForInBestIteration=" + highestCostForInBestIteration +
                ", highestCostForInBestIterationAgentName='" + highestCostForInBestIterationAgentName + '\'' +
                '}';
    }
}