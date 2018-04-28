package FinalProject.BL;

import FinalProject.BL.Agents.SmartHomeAgentBehaviour;
import FinalProject.BL.DataObjects.Problem;
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

    public void setAlgorithms(List<SmartHomeAgentBehaviour> algorithms)
    {
        this.algos = algorithms;
    }

    public void setProblems(List<Problem> problems)
    {
        this.problems = problems;
    }

    public Experiment createExperiment() throws RuntimeException
    {
        checkFieldsWereSet();//throws

        Experiment newlyCreatedExperiment = new Experiment(this.service, this.problems, this.algos);
        Experiment.maximumIterations = this.numOfIterations;
        return newlyCreatedExperiment;
    }

    private void checkFieldsWereSet()throws RuntimeException
    {
        if (service == null)
        {
            throw new RuntimeException("Could not create Experiment, service is null");
        }
        else if (problems == null || problems.size() == 0)
        {
            throw new RuntimeException("Could not create Experiment, no problems were added");
        }
        else if (algos == null || algos.size() == 0)
        {
            throw new RuntimeException("Could not create Experiment, no algorithms were added");
        }
        else if (numOfIterations <= 0)
        {
            throw new RuntimeException("Could not create Experiment, number of iterations was not set");
        }
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
