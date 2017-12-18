package FinalProject.BL.IterationData;

public class AgentIterationData {

    private int iterNum;
    private String agentName;
    private double price;
    private double powerConsumption;
    private double[] powerConsumptionPerDevice;

    public AgentIterationData(int iterNum, String agentName, double price, double powerCons, double [] powerConsPerDevice)
    {
        this.iterNum = iterNum;
        this.agentName = agentName;
        this.price = price;
        this.powerConsumption = powerCons;
        this.powerConsumptionPerDevice = powerConsPerDevice;
    }


}
