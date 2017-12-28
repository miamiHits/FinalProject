package FinalProject;

import FinalProject.BL.Agents.DSA;
import FinalProject.BL.DataCollection.AlgorithmProblemResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class UiHandler{

    private BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    private Service service;
    private final Object EXPERIMENT_RUN_WAITER = new Object();
    private List<AlgorithmProblemResult> experimentResults = null;

    public UiHandler(Service service)
    {
        this.service = service;
    }

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
        service.addAlgorithmsToExperiment(algoList, numOfIter);
        List<String> problem = new ArrayList<>();
        problem.add("dm_7_1_2");
        service.addProblemsToExperiment(problem);

        System.out.println("Starting Experiment!");
        service.runExperiment();
        showExperimentRunningScreen();
    }

    public void showExperimentRunningScreen() {
        System.out.println("Experiment it running");
        try
        {
            synchronized (EXPERIMENT_RUN_WAITER)
            {
                EXPERIMENT_RUN_WAITER.wait();
            }

            showResultScreen();
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    public void showResultScreen() {
        for (AlgorithmProblemResult res : experimentResults)
        {
            System.out.println(res.toString());
        }
        System.out.println('\n');

        showMainScreen();
    }

    public void notifyExperimentEnded(List<AlgorithmProblemResult> results) {
        System.out.println("Experiment Ended!");
        experimentResults = results;
        synchronized (EXPERIMENT_RUN_WAITER)
        {
            EXPERIMENT_RUN_WAITER.notifyAll();
        }
    }
}
