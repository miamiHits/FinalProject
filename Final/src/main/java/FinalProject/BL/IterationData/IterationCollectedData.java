package FinalProject.BL.IterationData;

public class IterationCollectedData extends AgentIterationData {

    private String problemId;
    private String algorithm;

    public String getProblemId() {
        return problemId;
    }

    public void setProblemId(String problemId) {
        this.problemId = problemId;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public IterationCollectedData(int iterNum, String agentName, double price, double[] powerConsPerDevice,
                                  String problemId, String algo ) {
        super(iterNum, agentName, price, powerConsPerDevice);
        this.problemId = problemId;
        this.algorithm = algo;
    }
}
