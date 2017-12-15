package FinalProject.BL.Agents;


import jade.core.behaviours.Behaviour;

//TODO: skeleton Only so Oded could write AlgoLoader
public abstract class SmartHomeAgentBehaviour extends Behaviour {

    String name;
    SmartHomeAgent agent;

    protected abstract void doIteration();
    protected abstract void sendIterationToCollector(); //TODO: missing params
}
