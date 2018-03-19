import FinalProject.BL.Agents.PropertyWithData;
import FinalProject.BL.Agents.SmartHomeAgentBehaviour;
import FinalProject.Utils;

import java.util.Map;

public class BehaviourToCompile extends SmartHomeAgentBehaviour {

    @Override
    protected void doIteration()
    {
        //nothing here
    }

    @Override
    protected void onTermination() {

    }

    @Override
    protected void generateScheduleForProp(PropertyWithData prop, double ticksToWork, Map<String, Double> sensorsToCharge) {

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
    protected long countIterationCommunication() {
        return -1;
    }

}