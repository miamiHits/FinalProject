package FinalProject.PL;

import FinalProject.BL.DataCollection.AlgorithmProblemResult;

import java.util.List;
import java.util.Map;

public interface UiHandlerInterface {
    void showMainScreen();

    void showExperimentRunningScreen();

    void showResultScreen(List<AlgorithmProblemResult> experimentResults, Map<String, Map<Integer, Long>>  probToAlgoTotalTime);

    void notifyExperimentEnded(List<AlgorithmProblemResult> results,Map<String, Map<Integer, Long>>  probToAlgoTotalTime);

    void notifyError(String msg);

    void algorithmProblemIterEnded(String algo, String problem, float changePercentage);
}
