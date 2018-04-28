package FinalProject.PL;

import FinalProject.BL.Agents.DSA;
import FinalProject.BL.Agents.SHMGM;
import FinalProject.BL.DataCollection.AlgorithmProblemResult;
import FinalProject.BL.DataCollection.StatisticsHandler;
import FinalProject.Config;
import FinalProject.DAL.*;
import FinalProject.Service;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.UI;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

@Push
@Theme("mytheme")
public class UiHandler extends UI implements UiHandlerInterface {

    private BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    public static Service service;
    public static Navigator navigator;
    public static ExperimentRunningPresenter experimentRunningPresenter;

    private ExperimentResultsPresenter resultsPresenter;
    protected static final String EXPERIMENT_CONFIGURATION = "EXPERIMENT_CONFIGURATION";
    protected static final String EXPERIMENT_RESULTS = "EXPERIMENT_RESULTS";
    protected static final String EXPERIMENT_RUNNING = "EXPERIMENT_RUNNING";

    private static final String RESULTS_PATH = Config.getStringPropery(Config.REPORTS_OUT_DIR).replaceAll("/", Matcher.quoteReplacement(Matcher.quoteReplacement(File.separator)));

    private static final Logger logger = Logger.getLogger(UiHandler.class);

    public UiHandler()
    {
        logger.debug("Uihandelr ceated");
        resultsPresenter = new ExperimentResultsPresenter();
        String jsonPath = Config.getStringPropery(Config.PROBLEMS_DIR);
        jsonPath.replaceAll("/", Matcher.quoteReplacement(Matcher.quoteReplacement(File.separator)));
        String algorithmsPath = Thread.currentThread().getContextClassLoader().getResource("FinalProject/BL/Agents/").getFile();
        algorithmsPath.replaceAll("/", Matcher.quoteReplacement(Matcher.quoteReplacement(File.separator)));

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

        navigator = new Navigator(this, this);

        // Create and register the views
        ExperimentConfigurationPresenter experimentConfigurationPresenter = new ExperimentConfigurationPresenter();
        experimentRunningPresenter = new ExperimentRunningPresenter();
        navigator.addView("", experimentConfigurationPresenter);
        navigator.addView(EXPERIMENT_RUNNING, experimentRunningPresenter);
        navigator.addView(EXPERIMENT_CONFIGURATION, experimentConfigurationPresenter);
        navigator.addView(EXPERIMENT_RESULTS, resultsPresenter);
    }

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
            } catch (IOException ignored)
            {
            }
            catch (NumberFormatException e)
            {
                System.out.println("Number of iterations must be a NUMBER!");
            }
        }

        List<String> algoList = new ArrayList<>();
        algoList.add(SHMGM.class.getSimpleName());
        algoList.add(DSA.class.getSimpleName());
        service.setAlgorithmsToExperiment(algoList, numOfIter);
        List<String> problem = new ArrayList<>();
        problem.add("dm_7_1_3");
        service.setProblemsToExperiment(problem);

        System.out.println("Starting Experiment!");
        service.runExperiment();
        showExperimentRunningScreen();
    }

    @Override
    public void showExperimentRunningScreen() {
        System.out.println("Experiment it running");
    }

    @Override
    public void showResultScreen(List<AlgorithmProblemResult> experimentResults, Map<String, Map<Integer, Long>>  probToAlgoTotalTime) {

        logger.debug("applying experiment into results to graphs and csv");
        for (AlgorithmProblemResult res : experimentResults)
        {
            logger.info(res.toString() + "\n\n");
        }

        Date date = new Date() ;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd--MM--yyyy_HH-mm") ;
        StatisticsHandler sth = new StatisticsHandler(experimentResults, probToAlgoTotalTime);

        //just for check the csv - we can change it later
        csvHandler csv = new csvHandler(RESULTS_PATH + dateFormat.format(date)+"_results.csv", sth.getTotalPowerConsumption());
        resultsPresenter.setPowerConsumptionGraph(sth.totalConsumption());
        resultsPresenter.setHighestAgentGrapthGrapth(sth.highestAgent());
        resultsPresenter.setLowestAgentGrapthGrapth(sth.lowestAgent());
        resultsPresenter.setAverageExperimentTime(sth.averageTime());
        resultsPresenter.setMessagesSentPerIteration(sth.messageSendPerIteration());
        resultsPresenter.setMessagesSizePerAlgo(sth.messagesSize());

        experimentRunningPresenter.enableGoToResScreenBtn();

        try {
            csv.saveExpirmentResult(experimentResults);
        } catch (IOException e) {
            logger.error("failed saving results to csv with exception ", e);
        }


    }

    @Override
    public void notifyExperimentEnded(List<AlgorithmProblemResult> results, Map<String, Map<Integer, Long>>  probToAlgoTotalTime)
    {
        logger.debug("Experiment Ended!");
        showResultScreen(results, probToAlgoTotalTime);
    }

    @Override
    public void notifyError(String msg)
    {//TODO gal display error message
        System.out.println(msg);
    }

    @Override
    public void algorithmProblemIterEnded(String algo, String problem, float changePercentage) {
        if (experimentRunningPresenter != null) {
            experimentRunningPresenter.incProgBar(problem, algo, changePercentage);
        }
        else
        {
            logger.warn("experimentRunningPresenter is null");
        }
    }

}
