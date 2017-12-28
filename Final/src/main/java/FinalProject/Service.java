package FinalProject;

import FinalProject.BL.Agents.SmartHomeAgentBehaviour;
import FinalProject.BL.DataCollection.AlgorithmProblemResult;
import FinalProject.BL.Experiment;
import FinalProject.BL.ExperimentBuilder;
import FinalProject.BL.Problems.Problem;
import FinalProject.DAL.DataAccessControllerInterface;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

public class Service extends Observable {

    private ExperimentBuilder experimentBuilder;
    private DataAccessControllerInterface dalController;
    public Experiment currExperiment;

    private final static Logger logger = Logger.getLogger(Service.class);

    public Service(DataAccessControllerInterface dalController)
    {
        logger.info("initialized");
        this.experimentBuilder = new ExperimentBuilder(this);
        this.dalController = dalController;
    }

    public void addAlgorithmsToExperiment(List<String> algorithmNames, int iterationNumber)
    {
        //TODO gal
        logger.info("algorithms added: " + algorithmNames.toString());
        List<SmartHomeAgentBehaviour> loadedAlgorithms = this.dalController.getAlgorithms(algorithmNames);
        this.experimentBuilder.addAlgorithms(loadedAlgorithms);
        this.experimentBuilder.setNumOfIterations(iterationNumber);
    }

    public void addProblemsToExperiment(List<String> problemNames)
    {
        //TODO gal
        logger.info("problems added: " + problemNames.toString());
        List<Problem> loadedProblems = this.dalController.getProblems(problemNames);
        this.experimentBuilder.addProblems(loadedProblems);
    }

    public void runExperiment()
    {
        //TODO gal
        this.currExperiment = this.experimentBuilder.createExperiment();
        this.currExperiment.runExperiment();
    }

    public void stopExperiment()
    {
        //TODO gal
        logger.info("experiment was stopped");
        this.currExperiment.stopExperiment();
    }

    public List<AlgorithmProblemResult> getExperimentResults()
    {
        List<AlgorithmProblemResult> results = new ArrayList<>();
        if (!this.currExperiment.experimentCompleted())
        {
            //decide what to return
        }
        //TODO gal
        return results;
    }

    public void experimentEnded(List<AlgorithmProblemResult> results)
    {
        setChanged();
        notifyObservers(results);
    }

    public void experimentEndedWithError(Exception e)
    {
        //TODO gal
        logger.error("error", e);
    }

    public void saveExperimentResult(List<AlgorithmProblemResult> results)
    {
        //TODO
    }
}
