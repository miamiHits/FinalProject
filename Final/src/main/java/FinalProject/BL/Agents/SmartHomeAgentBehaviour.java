package FinalProject.BL.Agents;


import jade.core.behaviours.Behaviour;

public abstract class SmartHomeAgentBehaviour extends Behaviour {

    public  String agentName;
    public SmartHomeAgent agent;

    protected abstract void doIteration();
    protected abstract void sendIterationToCollector(); //TODO: missing params
}
