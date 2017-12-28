package FinalProject.PL;

import FinalProject.BL.DataCollection.AlgorithmProblemResult;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

public interface UiHandlerInterface extends Observer {
    void showMainScreen();

    void showExperimentRunningScreen();

    void showResultScreen(List<AlgorithmProblemResult> arg);

    @Override
    void update(Observable o, Object arg);
}
