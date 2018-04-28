package FinalProject.PL;

import FinalProject.PL.UIEntities.ProblemAlgoPair;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.shared.Position;
import com.vaadin.ui.*;
import com.vaadin.ui.renderers.ComponentRenderer;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExperimentRunningPresenter extends Panel implements View{

    private Grid<ProblemAlgoPair> problemAlgoPairGrid;
    private Map<ProblemAlgoPair, ProgressBar> pairToProgressBarMap = new HashMap<>();;
    private ProgressBar mainProgBar = new ProgressBar(0.0f);
    private Button goToResScreenBtn;
    private boolean gridWasSet = false;

    private static final Logger logger = Logger.getLogger(ExperimentRunningPresenter.class);

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        logger.debug("enter");
        mainProgBar.setValue(0);

        goToResScreenBtn = new Button("Go to results screen!", clickEvent ->
        {
            logger.debug("clicked on \"Go to results screen!\" button");
            getUI().getNavigator().navigateTo(UiHandler.EXPERIMENT_RESULTS);
        });

        goToResScreenBtn.setEnabled(false);

        mainProgBar.setSizeFull();
        VerticalLayout layout = new VerticalLayout(problemAlgoPairGrid, mainProgBar, goToResScreenBtn);
        layout.setComponentAlignment(goToResScreenBtn, Alignment.MIDDLE_CENTER);
        setContent(layout);

    }

    public void incProgBar(String problemId, String algoId, float toIncBy) {
        ProblemAlgoPair problemAlgoPair = pairToProgressBarMap.keySet().stream()
                .filter(pair -> pair.getAlgorithm().equals(algoId) && pair.getProblemId().equals(problemId))
                .findFirst().orElse(null);
        if (problemAlgoPair != null) {
            ProgressBar progressBar = pairToProgressBarMap.get(problemAlgoPair);
            float current = progressBar.getValue();
            getUI().access(() -> {
                logger.debug(String.format("increasing progress bar for problem: %s algorithm: %s by %f to %f", problemId, algoId, toIncBy, current + toIncBy));
                progressBar.setValue(current + toIncBy);
                float mainBarNewVal = (float) (pairToProgressBarMap.values().stream()
                                            .mapToDouble(ProgressBar::getValue)
                                            .sum() / pairToProgressBarMap.values().size());
                mainProgBar.setValue(mainBarNewVal);

            });
        }
    }

    public void enableGoToResScreenBtn()
    {
        getUI().access(() -> {
            goToResScreenBtn.setEnabled(true);

            Notification notification = new Notification("Experiment Done!", "You can now view the results");
            notification.setPosition(Position.TOP_CENTER);
            notification.setStyleName(ValoTheme.NOTIFICATION_SUCCESS);
            notification.show(UI.getCurrent().getPage());

        });
    }

    public void setAlgorithmProblemPairs(List<ProblemAlgoPair> pairs) {
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
}
