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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AgentPrice that = (AgentPrice) o;

        if (Double.compare(that.price, price) != 0) return false;
        return agentName.equals(that.agentName);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = agentName.hashCode();
        temp = Double.doubleToLongBits(price);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

}
