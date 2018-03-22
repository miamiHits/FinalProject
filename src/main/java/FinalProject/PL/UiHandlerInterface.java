package FinalProject.PL;

import FinalProject.BL.DataCollection.AlgorithmProblemResult;

import java.util.List;

public interface UiHandlerInterface {
    void showMainScreen();

    void showExperimentRunningScreen();

    void showResultScreen(List<AlgorithmProblemResult> arg);

    void notifyExperimentEnded(List<AlgorithmProblemResult> results);

    void notifyError(String msg);

    void algorithmProblemIterEnded(String algo, String problem, float changePercentage);
}
