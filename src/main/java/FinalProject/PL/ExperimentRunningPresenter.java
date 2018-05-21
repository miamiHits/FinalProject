package FinalProject.PL;

import FinalProject.PL.UIEntities.ProblemAlgoPair;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.shared.Position;
import com.vaadin.ui.*;
import com.vaadin.ui.components.grid.HeaderRow;
import com.vaadin.ui.renderers.ComponentRenderer;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.log4j.Logger;
import org.vaadin.dialogs.ConfirmDialog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class ExperimentRunningPresenter extends Panel implements View{

    private Grid<ProblemAlgoPair> problemAlgoPairGrid;
    private Map<ProblemAlgoPair, ProgressBar> pairToProgressBarMap = new HashMap<>();
    private float currentRunningActualProgress = 0; //will contain the actual progress value of the current experiment since progress bar is updated on big milestones
    private ProgressBar mainProgBar = new ProgressBar(0.0f);
    private Button goToResScreenBtn;
    private Button stopBtn;
    private boolean gridWasSet = false;
    private int numOfIter = -1;
    private int numOfProblems = -1;
    private int numOfAlgos = -1;
    private Callable<Boolean> stopExperimentCallable = null;

    private static final Logger logger = Logger.getLogger(ExperimentRunningPresenter.class);

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        logger.debug("enter");
        mainProgBar.setValue(0);

        goToResScreenBtn = new Button("Go to results screen!", clickEvent -> {
            logger.debug("clicked on \"Go to results screen!\" button");
            getUI().getNavigator().navigateTo(UiHandler.EXPERIMENT_RESULTS);
        });
        goToResScreenBtn.addStyleName(ValoTheme.BUTTON_FRIENDLY);
        stopBtn = new Button("Stop Experiment!", clickEvent -> {
            logger.debug("clicked stop experiment");
            ConfirmDialog.show(getUI(), "Please confirm", "Stop experiment?", "Yes", "No",
                    (ConfirmDialog.Listener) confirmDialog -> {
                if (confirmDialog.isConfirmed() && stopExperimentCallable != null) {
                    try {
                        if (stopExperimentCallable.call()) {
                            getUI().getNavigator().navigateTo(UiHandler.EXPERIMENT_CONFIGURATION);
                        }
                        else {
                            new Notification("Could not stop experiment!", Notification.Type.ERROR_MESSAGE)
                                    .show(Page.getCurrent());
                        }
                    } catch (Exception e) {
                        new Notification("Could not stop experiment!", Notification.Type.ERROR_MESSAGE)
                                .show(Page.getCurrent());
                        e.printStackTrace();
                    }
                }
            });
        });
        stopBtn.addStyleName(ValoTheme.BUTTON_DANGER);

        goToResScreenBtn.setVisible(false);

        mainProgBar.setSizeFull();
        Label dataLbl = new Label("Number of iterations: " + numOfIter + ", Number of problems: "
                + numOfProblems + ", Number of Algorithms: " + numOfAlgos);
        VerticalLayout layout = new VerticalLayout(dataLbl, problemAlgoPairGrid, mainProgBar, goToResScreenBtn, stopBtn);
        layout.setComponentAlignment(goToResScreenBtn, Alignment.MIDDLE_CENTER);
        layout.setComponentAlignment(stopBtn, Alignment.MIDDLE_CENTER);
        setContent(layout);

    }

    public void incProgBar(String problemId, String algoId, float toIncBy) {
        ProblemAlgoPair problemAlgoPair = pairToProgressBarMap.keySet().stream()
                .filter(pair -> pair.getAlgorithm().equals(algoId) && pair.getProblemId().equals(problemId))
                .findFirst().orElse(null);
        if (problemAlgoPair != null) {
            ProgressBar progressBar = pairToProgressBarMap.get(problemAlgoPair);
            float currentProgressBarValue = progressBar.getValue();
            this.currentRunningActualProgress += toIncBy;
            if (this.currentRunningActualProgress - currentProgressBarValue >= 0.01) //update only when progressed 0.01
            {
                getUI().access(() -> {
                    logger.debug(String.format("increasing progress bar for problem: %s algorithm: %s by %f to %f", problemId, algoId, toIncBy, currentProgressBarValue + toIncBy));
                    progressBar.setValue(this.currentRunningActualProgress);
                    float mainBarNewVal = (float) (pairToProgressBarMap.values().stream()
                            .mapToDouble(ProgressBar::getValue)
                            .sum() / pairToProgressBarMap.values().size());
                    mainProgBar.setValue(mainBarNewVal);
                });
            }
        }
        else
        {
            logger.warn(String.format("could not find progress bor instance for problem - %s algorithm - %s", problemId, algoId));
        }
    }

    public void enableGoToResScreenBtn()
    {
        getUI().access(() -> {
            stopBtn.setVisible(false);
            goToResScreenBtn.setVisible(true);

            Notification notification = new Notification("Experiment Done!", "You can now view the results");
            notification.setPosition(Position.TOP_CENTER);
            notification.setStyleName(ValoTheme.NOTIFICATION_SUCCESS);
            notification.show(UI.getCurrent().getPage());

        });
    }

    public void setStopExperimentCallable(Callable<Boolean> callable) {
        stopExperimentCallable = callable;
    }

    public void setNumOfIter(int num) {
        numOfIter = num;
    }

    public void setAlgorithmProblemPairs(List<ProblemAlgoPair> pairs, int numOfAlgos, int numOfProblems) {
        this.numOfAlgos = numOfAlgos;
        this.numOfProblems = numOfProblems;
        pairToProgressBarMap.clear();
        pairs.forEach(pair -> pairToProgressBarMap.put(pair, new ProgressBar(0.0f)));
        initGrid();
    }

    private void initGrid() {
        if (!gridWasSet) {
            if (problemAlgoPairGrid == null) {
                problemAlgoPairGrid = new Grid<>(ProblemAlgoPair.class);
            }

            logger.debug("filling problems grid");

            problemAlgoPairGrid.setItems(pairToProgressBarMap.keySet());

            problemAlgoPairGrid.addColumn(pair -> {
                        ProgressBar progBar = pairToProgressBarMap.get(pair);
                        if (progBar == null)
                        {
                            logger.warn("could not find the relevant prgress bar, returning a new instance");
                            progBar = new ProgressBar(0.0f);
                            pairToProgressBarMap.put(pair, progBar);
                        }
                        progBar.setSizeFull();
                        return progBar;
                    },
                    new ComponentRenderer())
                    .setCaption("Progress")
                    .setId("progress");

            problemAlgoPairGrid.setSizeFull();
            gridWasSet = true;
        }
    }

    public void setProgressBarValue(String problemId, String algorithmId, float newValue, boolean applyOnGlobalProgressBar) {
        this.currentRunningActualProgress = newValue;
        ProblemAlgoPair problemAlgoPair = pairToProgressBarMap.keySet().stream()
                .filter(pair -> pair.getAlgorithm().equals(algorithmId) && pair.getProblemId().equals(problemId))
                .findFirst().orElse(null);
        if (problemAlgoPair != null) {
            ProgressBar progressBar = pairToProgressBarMap.get(problemAlgoPair);
            getUI().access(() -> {
                logger.debug(String.format("increasing progress bar for problem: %s algorithm: %s to %f", problemId, algorithmId, newValue));
                progressBar.setValue(this.currentRunningActualProgress);
                if (applyOnGlobalProgressBar)
                {
                    float mainBarNewVal = (float) (pairToProgressBarMap.values().stream()
                            .mapToDouble(ProgressBar::getValue)
                            .sum() / pairToProgressBarMap.values().size());
                    mainProgBar.setValue(mainBarNewVal);
                }
            });
        }
        else
        {
            logger.warn(String.format("could not find progress bar instance for problem - %s algorithm - %s", problemId, algorithmId));
        }
    }
}
