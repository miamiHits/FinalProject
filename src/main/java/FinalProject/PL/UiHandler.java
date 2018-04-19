package FinalProject.PL;

import FinalProject.BL.Agents.DSA;
import FinalProject.BL.Agents.SHMGM;
import FinalProject.BL.DataCollection.AlgorithmProblemResult;
import FinalProject.BL.DataCollection.StatisticsHandler;
import FinalProject.DAL.*;
import FinalProject.Service;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.UI;

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

    public UiHandler()
    {
        resultsPresenter = new ExperimentResultsPresenter();
        String jsonPath = "src/test/testResources/jsons";
        jsonPath.replaceAll("/", Matcher.quoteReplacement(Matcher.quoteReplacement(File.separator)));
        String algorithmsPath = "target/classes/FinalProject/BL/Agents";
        jsonPath.replaceAll("/", Matcher.quoteReplacement(Matcher.quoteReplacement(File.separator)));

        JsonLoaderInterface jsonLoader = new JsonLoader(jsonPath);
        AlgoLoaderInterface algorithmLoader = new AlgorithmLoader(algorithmsPath);
        DataAccessController dal = new DataAccessController(jsonLoader, algorithmLoader);
        service = new Service(dal);
        service.setObserver(this);

    }

    @Override
    protected void init(VaadinRequest request) {

        getPage().setTitle("Navigation Example");

        // Create a navigator to control the views
        navigator = new Navigator(this, this);

        // Create and register the views
        ExperimentConfigurationPresenter experimentConfigurationPresenter = new ExperimentConfigurationPresenter();
        experimentRunningPresenter = new ExperimentRunningPresenter();
//        experimentConfigurationPresenter.setExperimentRunningPresenter(experimentRunningPresenter);
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
        algoList.add(SHMGM.class.getName());
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
    public void showResultScreen(List<AlgorithmProblemResult> experimentResults, Map<String, Long> probToAlgoTotalTime) {
        for (AlgorithmProblemResult res : experimentResults)
        {
            System.out.println(res.toString());
        }
        System.out.println('\n');

        Date date = new Date() ;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd--MM--yyyy_HH-mm") ;
        //just for check the csv - we can change it later
        csvHandler csv = new csvHandler(dateFormat.format(date)+"_results.csv");
        StatisticsHandler sth = new StatisticsHandler(experimentResults, probToAlgoTotalTime);
        resultsPresenter.setPowerConsumptionGraph(sth.totalConsumption());
        resultsPresenter.setHighestAgentGrapthGrapth(sth.highestAgent());
        resultsPresenter.setLowestAgentGrapthGrapth(sth.lowestAgent());
        resultsPresenter.setAverageExperimentTime(sth.averageTime());
        resultsPresenter.setMessagesSentPerIteration(sth.messageSendPerIteration());
        //navigator.navigateTo(EXPERIMENT_RESULTS);

        experimentRunningPresenter.enableGoToResScreenBtn();

        try {
            csv.saveExpirmentResult(experimentResults);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void notifyExperimentEnded(List<AlgorithmProblemResult> results, Map<String, Long> probToAlgoTotalTime)
    {
        System.out.println("Experiment Ended!");
        showResultScreen(results, probToAlgoTotalTime);
    }

    @Override
    public void notifyError(String msg)
    {
        System.out.println(msg);
    }

    @Override
    public void algorithmProblemIterEnded(String algo, String problem, float changePercentage) {
        if (experimentRunningPresenter != null) {
            experimentRunningPresenter.incProgBar(problem, algo, changePercentage);
        }
    }


    @WebServlet(urlPatterns = "/*", name = "VaadinWebServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = FinalProject.PL.UiHandler.class, productionMode = false)
    public static class VaadinWebServlet extends VaadinServlet {

        @Override
        public void init() throws ServletException {
            super.init();

            // initializing simulator backend
            org.apache.log4j.BasicConfigurator.configure();
        }
    }

}
