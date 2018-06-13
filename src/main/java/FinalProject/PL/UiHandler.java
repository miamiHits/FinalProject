package FinalProject.PL;

import FinalProject.BL.Agents.SA;
import FinalProject.BL.Agents.SmartHomeAgentBehaviour;
import FinalProject.BL.DataCollection.AlgorithmProblemResult;
import FinalProject.BL.DataCollection.StatisticsHandler;
import FinalProject.Config;
import FinalProject.DAL.*;
import FinalProject.Service;
import FinalProject.VaadinWebServlet;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.ClientConnector;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.JavaScript;
import com.vaadin.ui.JavaScriptFunction;
import com.vaadin.ui.UI;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;

@Push
@Theme("mytheme")
public class UiHandler extends UI implements UiHandlerInterface, ClientConnector.DetachListener {

    private BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    public static Service service;
    public static ExperimentRunningPresenter currentRunningPresenter;

    private ExperimentRunningPresenter experimentRunningPresenter;
    private static ExperimentResultsPresenter resultsPresenter;
    private boolean isUIValid;

    protected static final String EXPERIMENT_CONFIGURATION = "EXPERIMENT_CONFIGURATION";
    protected static final String EXPERIMENT_RESULTS = "EXPERIMENT_RESULTS";
    protected static final String EXPERIMENT_RUNNING = "EXPERIMENT_RUNNING";

    private static final Logger logger = Logger.getLogger(UiHandler.class);

    public UiHandler()
    {
        Config.loadConfig();
        if (service != null)
        {
            logger.info("creating new UI(UiHandler) when one already exists! will not create new Service instances");
            return;
        }

        logger.debug("Uihandelr ceated");
        String jsonPath = Config.getStringPropery(Config.PROBLEMS_DIR);
        jsonPath.replaceAll("/", Matcher.quoteReplacement(Matcher.quoteReplacement(File.separator)));
        String algorithmsPath = new File(SmartHomeAgentBehaviour.class.getResource("").getFile()).getPath();

        JsonLoaderInterface jsonLoader = new JsonLoader(jsonPath);
        AlgoLoaderInterface algorithmLoader = new AlgorithmLoader(algorithmsPath);
        DataAccessController dal = new DataAccessController(jsonLoader, algorithmLoader);
        service = new Service(dal);
        service.setObserver(this);

    }

    @Override
    protected void init(VaadinRequest request) {

        Config.loadConfig();
        getPage().setTitle(Config.getStringPropery(Config.TITLE));
        addDetachListener(this);
        Navigator navigator = new Navigator(this, this);
        String initialNavigation = EXPERIMENT_CONFIGURATION;
        if (isUnwantedUI())
        {
            navigator.addView("", new InvalidAdditionalUIPresenter(navigator));
            return;
        }
        else if (UiHandler.currentRunningPresenter != null)
        {
            initialNavigation = EXPERIMENT_RUNNING;
            experimentRunningPresenter = UiHandler.currentRunningPresenter;
        }
        else
        {
            experimentRunningPresenter = new ExperimentRunningPresenter();
        }

        ////////////////////////////////////

        JavaScript.getCurrent().addFunction("browserIsLeaving",
                (JavaScriptFunction) arguments -> {
            logger.debug("browserIsLeaving");
            isUIValid = false;
        });
        Page.getCurrent().getJavaScript().execute("window.onbeforeunload = function (e) { var e = e || window.event; browserIsLeaving(); return; };");

        ////////////////////////////////////


        // Create and register the views
        ExperimentConfigurationPresenter experimentConfigurationPresenter = new ExperimentConfigurationPresenter(experimentRunningPresenter);
        if (resultsPresenter == null) {
            resultsPresenter = new ExperimentResultsPresenter(navigator);
        }
        navigator.addView(EXPERIMENT_RUNNING, experimentRunningPresenter);
        navigator.addView(EXPERIMENT_CONFIGURATION, experimentConfigurationPresenter);
        navigator.addView(EXPERIMENT_RESULTS, resultsPresenter);
        setMobileHtml5DndEnabled(true);
        this.isUIValid = true;
        navigator.navigateTo(initialNavigation);
    }

    /**
     * Used for CLI testing only.
     */
    @Override
    public void showMainScreen() {
        System.out.println("Showing main screen!");
        int numOfIter = -1;
        while (numOfIter < 0)
        {
            try
            {
                System.out.println("Please enter num of iterations");
                String input = reader.readLine();
                numOfIter = Integer.parseInt(input);
            } catch (IOException ignored) { }
            catch (NumberFormatException e) {
                System.out.println("Number of iterations must be a NUMBER!");
            }
        }

        List<String> algoList = new ArrayList<>();
        //algoList.add(SHMGM.class.getSimpleName());
        //algoList.add(DSA.class.getSimpleName());
        algoList.add(SA.class.getSimpleName());
        service.setAlgorithmsToExperiment(algoList, numOfIter);
        List<String> problem = new ArrayList<>();
        problem.add("dm_7_1_3");
        service.loadProblems(problem);

        System.out.println("Starting Experiment!");
        service.runExperiment();
    }

    @Override
    public void showResultScreen(List<AlgorithmProblemResult> experimentResults, Map<String, Map<Integer, Long>> probToAlgoTotalTime) {
        logger.debug("applying experiment into results to graphs and csv");
        for (AlgorithmProblemResult res : experimentResults)
        {
            logger.info(res.toString() + "\n\n");
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd--MM--yyyy_HH-mm") ;
        StatisticsHandler sth = new StatisticsHandler(experimentResults, probToAlgoTotalTime);

        Set<String> allAlgorithms = filterAlgoNames(experimentResults);
        for (String algo : allAlgorithms)
        {
            resultsPresenter.addPowerConsumptionGraph(algo, sth.totalConsumption(algo));
            resultsPresenter.addPowerConsumptionAnyTimeGraph(algo, sth.totalConsumptionAnyTime(algo));

            resultsPresenter.addHighestAgentGrapthGrapth(algo, sth.highestAgent(algo));
            resultsPresenter.addLowestAgentGrapthGrapth(algo, sth.lowestAgent(algo));
            resultsPresenter.addAverageExperimentTime(algo, sth.averageTime());
        }

        resultsPresenter.setMessagesSentPerIteration(sth.messageSendPerIteration());
        resultsPresenter.setMessagesSizePerAlgo(sth.messagesSize());
        resultsPresenter.setNumOfAlgos(experimentResults);
        experimentRunningPresenter.enableGoToResScreenBtn();

        if (!service.saveResults(sth.getTotalPowerConsumption(), sth.getTotalPowerConsumptionAnyTime(), sth.getHighestAgent(),
                sth.getLowestAgent(), sth.getAverageTimePerIteration(), sth.getTotalMessagesSize(), sth.getTotalAverageMessages(),experimentResults)) {
            logger.error("failed saving results to csv with exception ");
        }
    }

    private Set<String> filterAlgoNames(List<AlgorithmProblemResult> experimentResults) {
        Set<String> results = new HashSet<>();
        experimentResults.forEach(algo -> {
            String algoName = algo.getAlgorithm();
            if (!results.contains(algo)){
                results.add(algoName);
            }
        });

        return results;
    }

    @Override
    public void notifyExperimentEnded(List<AlgorithmProblemResult> results, Map<String, Map<Integer, Long>>  probToAlgoTotalTime)
    {
        logger.debug("Experiment Ended!");
        showResultScreen(results, probToAlgoTotalTime);
    }

    @Override
    public void algorithmProblemIterEnded(String algo, String problem, float changePercentage) {
        if (experimentRunningPresenter != null) {
            experimentRunningPresenter.incProgBar(problem, algo, changePercentage);
        }
        else {
            logger.warn("experimentRunningPresenter is null");
        }
    }

    @Override
    public void algorithmProblemComboRunEnded(String algorithm, String problem) {
        if (experimentRunningPresenter != null) {
            experimentRunningPresenter.setProgressBarValue(problem, algorithm, 1f, false);
            this.experimentRunningPresenter.currentRunningActualProgress = 0;
        }
        else
        {
            logger.warn("experimentRunningPresenter is null");
        }

    }

    @Override
    public void detach(DetachEvent event) {
        isUIValid = false;
    }

    private boolean isUnwantedUI() {
        boolean isSessionValid = (Boolean)getSession().getAttribute(VaadinWebServlet.IS_SESSION_VALID);
        boolean isSingularUI = getSession().getUIs()
                .stream()
                .allMatch(ui -> ui instanceof UiHandler && !((UiHandler) ui).isUIValid);
        return !isSessionValid || !isSingularUI;
    }
}
