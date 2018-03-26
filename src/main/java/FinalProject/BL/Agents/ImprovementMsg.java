package FinalProject.BL.Agents;

import jade.util.leap.Comparable;
import org.apache.log4j.Logger;

import java.io.Serializable;

public class ImprovementMsg implements Serializable, Comparable {
    private final static Logger logger = Logger.getLogger(ImprovementMsg.class);
    private String agentName;
    private double improvement;
    private int iterNum;
    private double[] imprevedSched;
    private double[] prevSched;

    public ImprovementMsg(String agentName, double improvement, int iterNum, double[] improvedSched, double[] prevSched) {
        this.agentName = agentName;
        this.improvement = improvement;
        this.iterNum = iterNum;
        this.imprevedSched = improvedSched;
        this.prevSched = prevSched;
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

    public double[] getImprevedSched() {
        return imprevedSched;
    }

    public void setImprevedSched(double[] imprevedSched) {
        this.imprevedSched = imprevedSched;
    }

    public double[] getPrevSched() {
        return prevSched;
    }

    public void setPrevSched(double[] prevSched) {
        this.prevSched = prevSched;
    }

    @Override
    public int compareTo(Object other) {

        if (other instanceof ImprovementMsg) {
            ImprovementMsg otherCast = (ImprovementMsg) other;
            if (improvement < 0) {
                logger.info(agentName + "'s impro is: NEGATIVE!! " + improvement);
            }
            else if (otherCast.improvement < 0) {
                logger.info("OTHER: " + otherCast.getAgentName() + "'s impro is: NEGATIVE!! " + otherCast.improvement);

            }
            double compare = this.improvement - otherCast.improvement;
            if (compare == 0) {
                final int strCompare = this.agentName.compareTo(otherCast.agentName);
                final int returned = strCompare * -1;
                logger.info(agentName + "'s impro is: " + improvement + " other's impro is:" + otherCast.improvement + " other's name is: " + otherCast.getAgentName() + ". returning BY NAME: " + returned);
                return returned;
            }
            final int returned = compare > 0 ? 1 : -1;
            logger.info(agentName + "'s impro is: " + improvement + " other's impro is:" + otherCast.improvement + ". returning: " + returned);
            return returned;
        }
//            logger.warn("ImprovementMsg.compareTo called to compare with non-ImprovementMsg, returning 0");
        return 0;
    }
}