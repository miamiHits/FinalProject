package FinalProject.PL.UIEntities;

public class ProblemAlgoPair {

    private String algorithm;
    private String problemId;

    public ProblemAlgoPair(String algorithm, String problemId) {
        this.algorithm = algorithm;
        this.problemId = problemId;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getProblemId() {
        return problemId;
    }

    public void setProblemId(String problemId) {
        this.problemId = problemId;
    }
}
