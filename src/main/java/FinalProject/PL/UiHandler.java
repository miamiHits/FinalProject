package FinalProject.PL;

import FinalProject.BL.Agents.DSA;
import FinalProject.BL.DataCollection.AlgorithmProblemResult;
import FinalProject.DAL.*;
import FinalProject.Service;
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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class UiHandler extends UI implements UiHandlerInterface {

    private BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    public static Service service;
    public static Navigator navigator;
    private ExperimentRunningPresenter experimentRunningPresenter;

    protected static final String EXPERIMENT_CONFIGURATION = "EXPERIMENT_CONFIGURATION";
    protected static final String EXPERIMENT_RESULTS = "EXPERIMENT_RESULTS";

    public UiHandler()
    {
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
        navigator.addView("", experimentRunningPresenter);
        navigator.addView(EXPERIMENT_CONFIGURATION, experimentConfigurationPresenter);
        navigator.addView(EXPERIMENT_RESULTS, new ExperimentResultsPresenter());
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
        algoList.add(DSA.class.getName());
        service.setAlgorithmsToExperiment(algoList, numOfIter);
        List<String> problem = new ArrayList<>();
        problem.add("dm_7_1_2");
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
    public void showResultScreen(List<AlgorithmProblemResult> experimentResults) {
        for (AlgorithmProblemResult res : experimentResults)
        {
            System.out.println(res.toString());
        }
        System.out.println('\n');

    }

    @Override
    public void notifyExperimentEnded(List<AlgorithmProblemResult> results)
    {
        System.out.println("Experiment Ended!");
        showResultScreen(results);
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
