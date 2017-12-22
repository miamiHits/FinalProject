package FinalProject.BL.DataCollection;

import java.util.LinkedList;
import java.util.List;

public class IterationAgentsPrice {
    private int iterationNum;
    private List<AgentPrice> agentsPrice;

    public IterationAgentsPrice(int iterationNum) {
        this.iterationNum = iterationNum;
        agentsPrice = new LinkedList<AgentPrice>();
    }

    public int getIterationNum() {
        return iterationNum;
    }

    public void setIterationNum(int iterationNum) {
        this.iterationNum = iterationNum;
    }

    public List<AgentPrice> getAgentsPrice() {
        return agentsPrice;
    }

    public void setAgentsPrice(List<AgentPrice> agentsPrice) {
        this.agentsPrice = agentsPrice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IterationAgentsPrice that = (IterationAgentsPrice) o;

        return iterationNum == that.iterationNum;
    }

    @Override
    public int hashCode() {
        return iterationNum;
    }


}
