package FinalProject;

import FinalProject.BL.Agents.SmartHomeAgentBehaviour;
import FinalProject.BL.DataCollection.AlgorithmProblemResult;
import FinalProject.BL.Experiment;
import FinalProject.BL.ExperimentBuilder;
import FinalProject.BL.DataObjects.Problem;
import FinalProject.DAL.DataAccessControllerInterface;
import FinalProject.PL.UiHandlerInterface;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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

    public List<String> getAvailableAlgorithms()
    {
        return this.dalController.getAvailableAlgorithms();
    }

    public Map<Integer, List<String>> getAvailableProblems()
    {
        return this.dalController.getAvailableProblems();
    }


    public void setAlgorithmsToExperiment(List<String> algorithmNames, int iterationNumber)
    {
        logger.info("algorithms added: " + algorithmNames.toString());
        List<SmartHomeAgentBehaviour> loadedAlgorithms = this.dalController.getAlgorithms(algorithmNames);
        this.experimentBuilder.setAlgorithms(loadedAlgorithms);
        this.experimentBuilder.setNumOfIterations(iterationNumber);
    }

    public void setProblemsToExperiment(List<String> problemNames)
    {
        logger.info("problems added: " + problemNames.toString());
        List<Problem> loadedProblems = this.dalController.getProblems(problemNames);
        this.experimentBuilder.setProblems(loadedProblems);
    }

    public void runExperiment()
    {
        try
        {
            this.currExperiment = this.experimentBuilder.createExperiment();
            this.currExperiment.runExperiment();
        } catch (RuntimeException e)
        {
            logger.error("runtime expection was cought", e);
            observer.notifyError(e.getMessage());
        }
    }

    public void stopExperiment()
    {
        logger.info("experiment was stopped");
        this.currExperiment.stopExperiment();
    }

    public void experimentEnded(List<AlgorithmProblemResult> results, Map<String, Map<Integer, Long>>  probToAlgoTotalTime)
    {
        observer.notifyExperimentEnded(results, probToAlgoTotalTime);
    }

    public void experimentEndedWithError(Exception e)
    {
        //TODO gal notify the ui
        logger.error("error", e);
    }

    public void algorithmProblemIterEnded(String algo, String problem, float changePercentage) {
        observer.algorithmProblemIterEnded(algo, problem, changePercentage);
    }

    public String addNewAlgo(String path, String fileName) {
        return dalController.addAlgorithmToSystem(path, fileName);
    }
}
