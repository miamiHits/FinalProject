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

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {

        problemAlgoPairGrid = new Grid<>(ProblemAlgoPair.class);
        initGrid();

        Button updateProgBar = new Button("update progress bar", clickEvent -> {
            pairToProgressBarMap.values().forEach(bar -> incProgBar("dm_7_shit", "DSA", 0.2f));
        });

        goToResScreenBtn = new Button("Go to results screen!", clickEvent ->
                getUI().getNavigator().navigateTo(UiHandler.EXPERIMENT_RESULTS));
        goToResScreenBtn.setEnabled(false);

        VerticalLayout layout = new VerticalLayout(problemAlgoPairGrid, updateProgBar, goToResScreenBtn);
        setContent(layout);

    }

    public void incProgBar(String problemId, String algoId, float toIncBy) {
        ProblemAlgoPair problemAlgoPair = pairToProgressBarMap.keySet().stream()
                .filter(pair -> pair.getAlgorithm().equals(algoId) && pair.getProblemId().equals(problemId))
                .findFirst().orElse(null);
        if (problemAlgoPair != null) {
            ProgressBar progressBar = pairToProgressBarMap.get(problemAlgoPair);
            float current = progressBar.getValue();
            progressBar.setValue(current + toIncBy);

            checkIfAllAlgosAreDone();
        }
    }

    private void checkIfAllAlgosAreDone() {
        long notDoneBars = pairToProgressBarMap.values().stream()
                .filter(bar -> bar.getValue() < 0.1f)
                .count();
        if (notDoneBars == 0) {
            goToResScreenBtn.setEnabled(true);
        }
    }

    public void setAlgorithmProblemPairs(List<ProblemAlgoPair> pairs) {
        pairs.forEach(pair -> pairToProgressBarMap.put(pair, new ProgressBar(0.0f)));
        initGrid();
    }

    private void initGrid() {
//        problemAlgoPairGrid.setItems(new ProblemAlgoPair("DSA", "dm_7_shit"));

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
    }
}
