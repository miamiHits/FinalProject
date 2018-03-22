package FinalProject.PL;

import FinalProject.BL.Agents.DSA;
import FinalProject.BL.DataCollection.AlgorithmProblemResult;
import FinalProject.DAL.csvHandler;
import FinalProject.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class UiHandler implements UiHandlerInterface {

    private BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    private Service service;

    public UiHandler(Service service)
    {
        this.service = service;
        service.setObserver(this);
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
        service.addAlgorithmsToExperiment(algoList, numOfIter);
        List<String> problem = new ArrayList<>();
        problem.add("dm_7_1_2");
        service.addProblemsToExperiment(problem);

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

        //just for check the csv - we can change it later
        csvHandler csv = new csvHandler("results.csv");

        try {
            csv.saveExpirmentResult(experimentResults);
        } catch (IOException e) {
            e.printStackTrace();
        }


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
}
