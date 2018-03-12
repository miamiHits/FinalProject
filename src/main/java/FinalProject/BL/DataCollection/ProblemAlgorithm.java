package FinalProject.BL.DataCollection;

public class ProblemAlgorithm {
    private String problemId;
    private String AlgorithmName;

    public ProblemAlgorithm(String problemId, String algorithmName) {
        this.problemId = problemId;
        AlgorithmName = algorithmName;
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
