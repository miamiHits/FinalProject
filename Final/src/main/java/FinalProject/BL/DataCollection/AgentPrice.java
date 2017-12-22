package FinalProject.BL.DataCollection;

public class AgentPrice {
    private String agentName;
    private double price;

    public AgentPrice(String agentName, double price) {
        this.agentName = agentName;
        this.price = price;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
