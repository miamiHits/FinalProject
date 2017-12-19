package FinalProject.BL.Agents;
import FinalProject.BL.Problems.*;
import jade.lang.acl.MessageTemplate;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class DSA extends SmartHomeAgentBehaviour {

    private static MessageTemplate expectedMessagesTemplate = MessageTemplate.MatchContent("ping");
    private boolean finished = false;
    public static final int START_TICK = 1;
    public final int FINAL_TICK = agent.getAgentData().getBackgroundLoad().length;


    public DSA(String agentName, SmartHomeAgent agent)
    {
        this.agentName = agentName;
        this.agent = agent;
    }
    @Override
    protected void doIteration() {
        Random r = new Random();
        List<Rule> rulesList = agent.getAgentData().getRules();
        //Sort by relation Value
        for(Rule rule : rulesList)
        {
            if (rule.isActive()==true)
            {
                switch (rule.getPrefix()) {
                    case BEFORE:
                        //take number in range (1, relationVal)
                       int tickToWork = r.nextInt((int)(rule.getRelationValue() - START_TICK) + 1) + START_TICK;
                        // ???
                       break;
                    case AFTER:
                        for (int i = (int) Math.floor(rule.getRelationValue()); i<=12; i++)
                        {
                            addToSched(i, (Actuator) rule.getDevice());
                        }
                        break;
                    case AT:
                        addToSched((int) Math.floor(rule.getRelationValue()), (Actuator) rule.getDevice());

                    default:
                        throw new NotImplementedException();
                }

            }
            else{
                int tickToWork = r.nextInt((FINAL_TICK - START_TICK) + 1) + START_TICK;
                addToSched(tickToWork, (Actuator) rule.getDevice());

            }
        }
    }

    private void addToSched(int tick, Actuator actuator)
    {
        List<Actuator> l = agent.getMySchedule().get(tick);
        l.add(actuator);
        agent.getMySchedule().put(tick, l);
    }
    private void consume(Rule r) {
    }

    private boolean checkIfNeedToConsume(double relVal, Effect e) {

           String sensName = e.getProperty();
           Sensor s = agent.getAgentData().getSensors().stream()
                   .filter(x->x.getName().equals(sensName)).findAny().get();

       return relVal == s.getCurrentState() ? false : true;
    }




    @Override
    protected void sendIterationToCollector() {

    }

    @Override
    public void action() {
        this.agent.printLog("checking my schedule");
        this.agent.buildSchedule();
        doIteration();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            agent.printLog(e.getMessage());
        }
    }

    @Override
    public boolean done() {
        return finished;
    }
}
