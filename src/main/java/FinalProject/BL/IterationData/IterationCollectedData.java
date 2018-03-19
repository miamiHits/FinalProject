package FinalProject.BL.IterationData;

import java.util.Set;

public class IterationCollectedData extends AgentIterationData {

    private String problemId;
    private String algorithm;
    private Set<String> neighborhood;
    private double ePeak;
    private long messagesSize;

    public IterationCollectedData(int iterNum, String agentName, double price, double[] powerConsPerDevice,
                                  String problemId, String algo, Set<String> neighborhood, double epeak, long messagesSize ) {
        super(iterNum, agentName, price, powerConsPerDevice);
        this.problemId = problemId;
        this.algorithm = algo;
        this.neighborhood = neighborhood;
        this.ePeak = epeak;
        this.messagesSize = messagesSize;
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

    public double getePeak() {
        return ePeak;
    }

    public void setePeak(double ePeak) {
        this.ePeak = ePeak;
    }

    public long getMessagesSize() {
        return messagesSize;
    }

    public void setMessagesSize(long messagesSize) {
        this.messagesSize = messagesSize;
    }

}
