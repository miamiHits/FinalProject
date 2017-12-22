package FinalProject.BL.DataCollection;

import java.util.Map;

public class AlgorithmProblemResult {
    private String problem;
    private String algorithm;
    private Map<Integer, Double> avgPricePerIteration;
    private int iterationsTillBestPrice;
    private double lowestCostInBestIteration;
    private String lowestCostInBestIterationAgentName;
    private double highestCostInBestIteration;
    private String highestCostInBestIterationAgentName;

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
}