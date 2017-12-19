package FinalProject.BL.IterationData;

public class IterationCollectedData extends AgentIterationData {

    private String problemId;
    private String algorithm;
    public IterationCollectedData(int iterNum, String agentName, double price, double powerCons, double[] powerConsPerDevice,
                                  String problemId, String algo ) {
        super(iterNum, agentName, price, powerConsPerDevice);
        this.problemId = problemId;
        this.algorithm = algo;
    }
}
