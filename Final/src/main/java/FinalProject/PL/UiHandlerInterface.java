package FinalProject.PL;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by oded on 12/28/2017.
 */
public interface UiHandlerInterface extends Observer {
    void showMainScreen();

    void showExperimentRunningScreen();

    void showResultScreen();

    @Override
    void update(Observable o, Object arg);
}
