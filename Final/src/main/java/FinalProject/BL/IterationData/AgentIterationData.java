package FinalProject.BL.IterationData;

public class AgentIterationData {

    private int iterNum;
    private String agentName;
    private double price;
    private double[] powerConsumptionPerTick;

    public AgentIterationData(int iterNum, String agentName, double price, double[] powerConsPerDevice)
    {
        this.iterNum = iterNum;
        this.agentName = agentName;
        this.price = price;
        this.powerConsumptionPerTick = powerConsPerDevice;
    }


}
