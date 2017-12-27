package FinalProject.BL.DataCollection;

import java.util.HashMap;
import java.util.Map;

public class AlgorithmProblemResult {
    private String problem;
    private String algorithm;
    private Map<Integer, Double> avgPricePerIteration;
    private int iterationsTillBestPrice;
    private double lowestCost;
    private double lowestCostInBestIteration;
    private String lowestCostInBestIterationAgentName;
    private double highestCostInBestIteration;
    private String highestCostInBestIterationAgentName;

    public AlgorithmProblemResult(ProblemAlgorithm probAlgo) {
        problem = probAlgo.getProblemId();
        algorithm = probAlgo.getAlgorithmName();
        avgPricePerIteration = new HashMap<Integer, Double>();
        iterationsTillBestPrice = 0;
        lowestCost = Double.MAX_VALUE;
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

    public double getLowestCostInBestIteration() {
        return lowestCostInBestIteration;
    }

    public void setLowestCostInBestIteration(double lowestCostInBestIteration) {
        this.lowestCostInBestIteration = lowestCostInBestIteration;
    }

    public String getLowestCostInBestIterationAgentName() {
        return lowestCostInBestIterationAgentName;
    }

    public void setLowestCostInBestIterationAgentName(String lowestCostInBestIterationAgentName) {
        this.lowestCostInBestIterationAgentName = lowestCostInBestIterationAgentName;
    }

    public double getHighestCostInBestIteration() {
        return highestCostInBestIteration;
    }

    public void setHighestCostInBestIteration(double highestCostInBestIteration) {
        this.highestCostInBestIteration = highestCostInBestIteration;
    }

    public String getHighestCostInBestIterationAgentName() {
        return highestCostInBestIterationAgentName;
    }

    public void setHighestCostInBestIterationAgentName(String highestCostInBestIterationAgentName) {
        this.highestCostInBestIterationAgentName = highestCostInBestIterationAgentName;
    }

    public double getLowestCost() {
        return lowestCost;
    }

    public void setLowestCost(double lowestCost) {
        this.lowestCost = lowestCost;
    }

}