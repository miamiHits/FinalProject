package FinalProject;

import FinalProject.BL.Agents.SmartHomeAgentBehaviour;
import FinalProject.BL.DataCollection.AlgorithmProblemResult;
import FinalProject.BL.Experiment;
import FinalProject.BL.ExperimentBuilder;
import FinalProject.BL.DataObjects.Problem;
import FinalProject.DAL.DataAccessControllerInterface;
import FinalProject.PL.UiHandlerInterface;
import org.apache.log4j.Logger;

import java.util.List;

public class Service {

    private ExperimentBuilder experimentBuilder;
    private DataAccessControllerInterface dalController;
    public Experiment currExperiment;
    private UiHandlerInterface observer;

    private final static Logger logger = Logger.getLogger(Service.class);

    public Service(DataAccessControllerInterface dalController)
    {
        logger.info("initialized");
        this.experimentBuilder = new ExperimentBuilder(this);
        this.dalController = dalController;
    }

    public void setObserver(UiHandlerInterface ui)
    {
        observer = ui;
    }

    public ExperimentBuilder getExperimentBuilder()
    {
        return experimentBuilder;
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
        try
        {
            this.currExperiment = this.experimentBuilder.createExperiment();
            this.currExperiment.runExperiment();
        } catch (RuntimeException e)
        {
            observer.notifyError(e.getMessage());
        }
    }

    public void stopExperiment()
    {
        //TODO gal
        logger.info("experiment was stopped");
        this.currExperiment.stopExperiment();
    }

    public void experimentEnded(List<AlgorithmProblemResult> results)
    {
        observer.notifyExperimentEnded(results);
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
