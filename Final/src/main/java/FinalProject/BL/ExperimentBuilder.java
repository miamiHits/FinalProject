package FinalProject.BL;

import FinalProject.BL.Agents.SmartHomeAgentBehaviour;
import FinalProject.BL.Problems.Problem;
import FinalProject.Service;

import java.util.List;

public class ExperimentBuilder {
    private int numOfIterations;
    private List<Problem> problems;
    private List<SmartHomeAgentBehaviour> algos;
    private Service service;

    public ExperimentBuilder(Service service)
    {
        this.service = service;
    }

    public void setNumOfIterations(int newVal)
    {
        this.numOfIterations = newVal;
    }

    public void addAlgorithms(List<SmartHomeAgentBehaviour> algorithms)
    {
        //TODO gal
        this.algos = algorithms;
    }

    public void addProblems(List<Problem> problems)
    {
        //TODO gal
        this.problems = problems;
    }

    public Experiment createExperiment()
    {
        //TODO gal
        Experiment newlyCreatedExperiment = new Experiment(this.service, this.problems, this.algos);
        Experiment.maximumIterations = this.numOfIterations;
        return newlyCreatedExperiment;
    }

    public int getNumOfIterations()
    {
        return numOfIterations;
    }

    public List<Problem> getProblems()
    {
        return problems;
    }

    public List<SmartHomeAgentBehaviour> getAlgos()
    {
        return algos;
    }
}
