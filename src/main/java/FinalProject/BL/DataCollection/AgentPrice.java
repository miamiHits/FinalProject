package FinalProject.BL.DataCollection;

import java.util.Arrays;

public class AgentPrice {
    private String agentName;
    private double price;
    private double[] schedule;
    private long msgLength;

    public AgentPrice(String agentName, double price, double[] schedule, long msgLength) {
        this.agentName = agentName;
        this.price = price;
        this.schedule = schedule;
        this.msgLength = msgLength;
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

    public double[] getSchedule() {
        return schedule;
    }

    public void setSchedule(double[] schedule) {
        this.schedule = schedule;
    }

    public long getMsgLength() {
        return msgLength;
    }

    public void setMsgLength(long msgLength) {
        this.msgLength = msgLength;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AgentPrice that = (AgentPrice) o;

        if (Double.compare(that.price, price) != 0) return false;
        if (msgLength != that.msgLength) return false;
        if (agentName != null ? !agentName.equals(that.agentName) : that.agentName != null) return false;
        return Arrays.equals(schedule, that.schedule);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = agentName != null ? agentName.hashCode() : 0;
        temp = Double.doubleToLongBits(price);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + Arrays.hashCode(schedule);
        result = 31 * result + (int) (msgLength ^ (msgLength >>> 32));
        return result;
    }

}
