package FinalProject.BL.DataCollection;

public class ProblemAlgorithm {
    private String problemId;
    private String AlgorithmName;
    private int bestIteration;
    private double costInBestIteration;

    public ProblemAlgorithm(String problemId, String algorithmName) {
        this.problemId = problemId;
        AlgorithmName = algorithmName;
        bestIteration = 0;
        costInBestIteration = 0;
    }

    public boolean isBestIteration(int iterationNum, double cost){
        if (cost < costInBestIteration){
            costInBestIteration = cost;
            bestIteration = iterationNum;
            return true;
        }
        return false;
    }

    public String getProblemId() {
        return problemId;
    }

    public void setProblemId(String problemId) {
        this.problemId = problemId;
    }

    public String getAlgorithmName() {
        return AlgorithmName;
    }

    public void setAlgorithmName(String algorithmName) {
        AlgorithmName = algorithmName;
    }

    public int getBestIteration() {
        return bestIteration;
    }

    public void setBestIteration(int bestIteration) {
        this.bestIteration = bestIteration;
    }

    public double getCostInBestIteration() {
        return costInBestIteration;
    }

    public void setCostInBestIteration(double costInBestIteration) {
        this.costInBestIteration = costInBestIteration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProblemAlgorithm that = (ProblemAlgorithm) o;

        if (!problemId.equals(that.problemId)) return false;
        return AlgorithmName.equals(that.AlgorithmName);
    }

    @Override
    public int hashCode() {
        int result = problemId.hashCode();
        result = 31 * result + AlgorithmName.hashCode();
        return result;
    }

}
