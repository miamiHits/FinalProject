package FinalProject.BL.Agents;

import jade.util.leap.Comparable;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

public class ImprovementMsg implements Serializable, Comparable {
    private final static Logger logger = Logger.getLogger(ImprovementMsg.class);
    private String agentName;
    private double improvement;
    private double[] imprevedSched;
    private double[] prevSched;

    public ImprovementMsg(String agentName, double improvement, double[] improvedSched, double[] prevSched) {
        this.agentName = agentName;
        this.improvement = improvement;
        this.imprevedSched = improvedSched;
        this.prevSched = prevSched;
    }

    private double calcPrice(double[] sched, double[] priceScheme) {
        if (sched.length == priceScheme.length) {
            double sum = 0;
            for (int i = 0; i < sched.length; i++) {
                sum += sched[i] * priceScheme[i];
            }
            return sum;
        }
        logger.warn("sched.length != priceScheme.length! returning -1");
        return -1;
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
            double compare = this.improvement - otherCast.improvement;
            if (compare == 0) {
                final int strCompare = this.agentName.compareTo(otherCast.agentName);
                return strCompare * -1;
            }
            return compare > 0 ? 1 : -1;
        }
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImprovementMsg that = (ImprovementMsg) o;
        return Double.compare(that.improvement, improvement) == 0 &&
                agentName.equals(that.agentName) &&
                Arrays.equals(imprevedSched, that.imprevedSched) &&
                Arrays.equals(prevSched, that.prevSched);
    }

    @Override
    public int hashCode() {

        int result = Objects.hash(agentName, improvement);
        result = 31 * result + Arrays.hashCode(imprevedSched);
        result = 31 * result + Arrays.hashCode(prevSched);
        return result;
    }
}