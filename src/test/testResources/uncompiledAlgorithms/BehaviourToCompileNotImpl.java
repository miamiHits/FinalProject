package FinalProject.BL.Agents;

import java.util.List;
import java.util.Map;

public class BehaviourToCompileNotImpl extends SmartHomeAgentBehaviour {

    @Override
    protected void doIteration() {

    }

    @Override
    protected void onTermination() {

    }

    @Override
    protected void generateScheduleForProp(PropertyWithData prop, double ticksToWork, Map<String, Integer> sensorsToCharge, boolean randomSched) {

    }

    @Override
    protected void sendIterationToCollector()
    {
        //nothing here
    }

    @Override
    public SmartHomeAgentBehaviour cloneBehaviour() {
        return null;
    }


    @Override
    public void action()
    {

    }

    @Override
    public boolean done()
    {
        return false;
    }

    @Override
    protected void countIterationCommunication() {
    }

    @Override
    protected double calcImproveOptionGrade(double[] newPowerConsumption, List<double[]> allScheds) {
        return -1;
    }

}