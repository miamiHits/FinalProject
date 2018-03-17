package FinalProject.PL;

import FinalProject.BL.DataCollection.AlgorithmProblemResult;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import java.util.List;

public class GraphicalUIHandler implements UiHandlerInterface{
    @Override
    public void showMainScreen() {

    }

    @Override
    public void showExperimentRunningScreen() {

    }

    @Override
    public void showResultScreen(List<AlgorithmProblemResult> arg) {

    }

    @Override
    public void notifyExperimentEnded(List<AlgorithmProblemResult> results) {

    }

    @Override
    public void notifyError(String msg) {

    }


    @WebServlet(urlPatterns = "/*", name = "VaadinWebServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = FinalProject.PL.ExperimentConfigurationPresenter.class, productionMode = false)
    public static class VaadinWebServlet extends VaadinServlet {
        @Override
        public void init() throws ServletException {
            super.init();
        }
    }
}
