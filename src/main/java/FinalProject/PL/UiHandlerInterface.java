package FinalProject.PL;

import FinalProject.BL.DataCollection.AlgorithmProblemResult;

import java.util.List;
import java.util.Map;

public interface UiHandlerInterface {
    /**
     * Used for CLI testing only.
     */
    void showMainScreen();

    void showResultScreen(List<AlgorithmProblemResult> experimentResults, Map<String, Map<Integer, Long>>  probToAlgoTotalTime);

    void notifyExperimentEnded(List<AlgorithmProblemResult> results,Map<String, Map<Integer, Long>>  probToAlgoTotalTime);

    void algorithmProblemIterEnded(String algo, String problem, float changePercentage);

    void algorithmProblemComboRunEnded(String algorithm, String problem);
}
