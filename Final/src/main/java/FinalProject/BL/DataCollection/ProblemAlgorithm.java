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

}
