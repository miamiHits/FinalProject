package FinalProject.BL.Agents;
import jade.lang.acl.MessageTemplate;

public class DSA extends SmartHomeAgentBehaviour {

    private static MessageTemplate expectedMessagesTemplate = MessageTemplate.MatchContent("ping");
    private boolean finished = false;

    public DSA(String agentName, SmartHomeAgent agent)
    {
        this.agentName = agentName;
        this.agent = agent;
    }
    @Override
    protected void doIteration() {

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
