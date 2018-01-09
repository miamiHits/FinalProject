package FinalProject.BL.IterationData;

import java.util.Set;

public class IterationCollectedData extends AgentIterationData {

    private String problemId;
    private String algorithm;
    private Set<String> neighborhood;

    public IterationCollectedData(int iterNum, String agentName, double price, double[] powerConsPerDevice,
                                  String problemId, String algo ) {
        super(iterNum, agentName, price, powerConsPerDevice);
        this.problemId = problemId;
        this.algorithm = algo;
    }

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

    public void setNeighborhood(Set<String> neighborhood) {
        this.neighborhood = neighborhood;
    }

    public Set<String> getNeighborhood() {
        return neighborhood;
    }
}
