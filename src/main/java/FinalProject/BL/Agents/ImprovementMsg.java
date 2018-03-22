package FinalProject.BL.Agents;

import jade.util.leap.Comparable;

import java.io.Serializable;

public class ImprovementMsg implements Serializable, Comparable {
    private String agentName;
    private double improvement;
    private int iterNum;

    public ImprovementMsg(String agentName, double improvement, int iterNum) {
        this.agentName = agentName;
        this.improvement = improvement;
        this.iterNum = iterNum;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public double getImprovement() {
        return improvement;
    }

    public void setImprovement(double improvement) {
        this.improvement = improvement;
    }

    public int getIterNum() {
        return iterNum;
    }

    public void setIterNum(int iterNum) {
        this.iterNum = iterNum;
    }

    @Override
    public int compareTo(Object other) {
        if (other instanceof ImprovementMsg) {
            ImprovementMsg otherCast = (ImprovementMsg) other;
            double compare = Math.abs(this.improvement) - Math.abs(otherCast.improvement);
            if (compare == 0) {
                return this.agentName.compareTo(otherCast.agentName);
            }
            return compare > 0 ? 1 : -1;
        }
//            logger.warn("ImprovementMsg.compareTo called to compare with non-ImprovementMsg, returning 0");
        return 0;
    }
}