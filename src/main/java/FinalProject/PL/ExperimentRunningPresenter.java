package FinalProject.PL;

import FinalProject.PL.UIEntities.ProblemAlgoPair;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.*;
import com.vaadin.ui.renderers.ComponentRenderer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExperimentRunningPresenter extends Panel implements View{

    private Grid<ProblemAlgoPair> problemAlgoPairGrid;
    private final Map<ProblemAlgoPair, ProgressBar> pairToProgressBarMap = new HashMap<>();
    private Button goToResScreenBtn;
    private boolean gridWasSet = false;

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {

        goToResScreenBtn = new Button("Go to results screen!", (clickEvent) ->
        {

            getUI().getNavigator().navigateTo(UiHandler.EXPERIMENT_RESULTS);
        });

        goToResScreenBtn.setEnabled(false);

        VerticalLayout layout = new VerticalLayout(problemAlgoPairGrid, goToResScreenBtn);
        setContent(layout);

    }

    public void incProgBar(String problemId, String algoId, float toIncBy) {
        ProblemAlgoPair problemAlgoPair = pairToProgressBarMap.keySet().stream()
                .filter(pair -> pair.getAlgorithm().equals(algoId) && pair.getProblemId().equals(problemId))
                .findFirst().orElse(null);
        if (problemAlgoPair != null) {
            ProgressBar progressBar = pairToProgressBarMap.get(problemAlgoPair);
            float current = progressBar.getValue();
            getUI().access(new Runnable() {
                @Override
                public void run() {
                    progressBar.setValue(current + toIncBy);
                }
            });
        }
    }

    public void enableGoToResScreenBtn()
    {
        getUI().access(new Runnable() {
            @Override
            public void run() {
                goToResScreenBtn.setEnabled(true);
            }
        });
    }

    public void setAlgorithmProblemPairs(List<ProblemAlgoPair> pairs) {
        pairs.clear();
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
