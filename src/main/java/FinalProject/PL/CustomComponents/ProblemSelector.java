package FinalProject.PL.CustomComponents;

import FinalProject.PL.UIEntities.SelectedProblem;
import com.vaadin.data.TreeData;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.TreeDataProvider;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.*;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ProblemSelector extends CustomComponent {

    private Set<SelectedProblem> selectedProblems;
    private final VerticalLayout mainLayout = new VerticalLayout();
    private Grid<SelectedProblem> selectedProblemGrid;
    private Tree<String> problemTree;


    public ProblemSelector(Set<SelectedProblem> selectedProblems, Supplier<Map<Integer, List<String>>> problemsSupplier) {
        Panel mainPanel = new Panel();
        mainPanel.setCaption("Select Your Problems");
        mainPanel.setContent(mainLayout);
        setCompositionRoot(mainPanel);
        this.selectedProblems = selectedProblems;
        generateProblemsSection(problemsSupplier);

        mainPanel.setSizeUndefined();
        setSizeUndefined();
    }

    private void generateProblemsSection(Supplier<Map<Integer, List<String>>> problemsSupplier) {
        problemTree = new Tree<>("Available Problems");
        selectedProblemGrid = new Grid<>(SelectedProblem.class);

        Map<Integer, List<String>> sizeToNameMap = initTree(problemsSupplier, problemTree, selectedProblemGrid);

        initGrid();

        Button addAllProblemsBtn = new Button("Add All");
        addAllProblemsBtn.addClickListener(generateAddAllClickListener(sizeToNameMap, selectedProblemGrid));

        GridLayout treeGridLayout = new GridLayout(5, 2);
        treeGridLayout.setSpacing(true);
        treeGridLayout.addComponent(problemTree, 0, 0);
        treeGridLayout.addComponent(selectedProblemGrid, 1, 0, 4, 0);
        treeGridLayout.addComponent(addAllProblemsBtn, 4, 1);
        treeGridLayout.setComponentAlignment(addAllProblemsBtn, Alignment.BOTTOM_RIGHT);

        setComponentSizes();

        mainLayout.addComponents(treeGridLayout);
    }

    private void setComponentSizes() {
        selectedProblemGrid.setHeight(300, Unit.PIXELS);
        problemTree.setWidth(200, Unit.PIXELS);
    }

    private void initGrid() {
            selectedProblemGrid.setCaption("Selected Problems");
            selectedProblemGrid.setDataProvider(DataProvider.ofCollection(selectedProblems));
            selectedProblemGrid.setColumnOrder("size", "name");
            selectedProblemGrid.sort("size", SortDirection.ASCENDING);
            selectedProblemGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
            selectedProblemGrid.addItemClickListener(itemClick -> {
            SelectedProblem item = itemClick.getItem();
            selectedProblems.remove(item);
            refreshGrid();
        });
    }

    private Map<Integer, List<String>> initTree(Supplier<Map<Integer, List<String>>> problemsSupplier, Tree<String> problemTree, Grid<SelectedProblem> selectedProblemGrid) {
        TreeData<String> treeData = new TreeData<>();
        problemTree.setDataProvider(new TreeDataProvider<>(treeData));
        Map<Integer, List<String>> sizeToNameMap = problemsSupplier.get();
        sizeToNameMap.entrySet().stream()
                .sorted(Comparator.comparingInt(Map.Entry::getKey))
                .forEach(entry -> {
                    String size = String.valueOf(entry.getKey());
                    List<String> names = entry.getValue();
                    treeData.addRootItems(size);
                    treeData.addItems(size, names);
                    problemTree.collapse();
                });
        problemTree.setContentMode(ContentMode.TEXT);
        problemTree.addItemClickListener(itemClick -> {

            String item = itemClick.getItem();
            List<String> children = treeData.getChildren(item);
            //is a specific problem
            if (children == null || children.size() == 0) {
                int parent = Integer.parseInt(treeData.getParent(item));
                SelectedProblem selected = new SelectedProblem(item, parent);
                selectedProblems.add(selected);
            }
            //is a folder
            else {
                List<SelectedProblem> toAdd = children.stream()
                        .map(child -> new SelectedProblem(child, Integer.parseInt(item)))
                        .collect(Collectors.toList());
                selectedProblems.addAll(toAdd);
            }
            refreshGrid();
        });
        return sizeToNameMap;
    }

    private void refreshGrid() {
        this.selectedProblemGrid.getDataProvider().refreshAll();
        this.selectedProblemGrid.sort("size", SortDirection.ASCENDING);
    }

    private Button.ClickListener generateAddAllClickListener(Map<Integer, List<String>> map, Grid<SelectedProblem> grid) {
        return (Button.ClickListener) event ->
                map.forEach((size, names) ->
                        names.forEach(name -> {
                            SelectedProblem selected = new SelectedProblem(name, size);
                            selectedProblems.add(selected);
                            refreshGrid();
                        }));
    }
}
