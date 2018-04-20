package FinalProject.PL;

import FinalProject.PL.UIEntities.ProblemAlgoPair;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.shared.Position;
import com.vaadin.ui.*;
import com.vaadin.ui.renderers.ComponentRenderer;
import com.vaadin.ui.themes.ValoTheme;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExperimentRunningPresenter extends Panel implements View{

    private Grid<ProblemAlgoPair> problemAlgoPairGrid;
    private Map<ProblemAlgoPair, ProgressBar> pairToProgressBarMap = new HashMap<>();;
    private ProgressBar mainProgBar = new ProgressBar(0.0f);
    private Button goToResScreenBtn;
    private boolean gridWasSet = false;

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        mainProgBar.setValue(0);

        goToResScreenBtn = new Button("Go to results screen!", clickEvent ->
            getUI().getNavigator().navigateTo(UiHandler.EXPERIMENT_RESULTS)
        );

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

            problemAlgoPairGrid.setItems(pairToProgressBarMap.keySet());

            problemAlgoPairGrid.addColumn(pair -> {
                        ProgressBar progBar = new ProgressBar(0.0f);
                        progBar.setSizeFull();
                        pairToProgressBarMap.put(pair, progBar);
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
