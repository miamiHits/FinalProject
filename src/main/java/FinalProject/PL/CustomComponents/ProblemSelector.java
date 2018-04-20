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


    public ProblemSelector(Set<SelectedProblem> selectedProblems, Supplier<Map<Integer, List<String>>> problemsSupplier) {
        Panel mainPanel = new Panel();
        mainPanel.setCaption("Select Your Problems");
        mainPanel.setContent(mainLayout);
        setCompositionRoot(mainPanel);
        this.selectedProblems = selectedProblems;
        generateProblemsSection(problemsSupplier);

        mainLayout.setSizeUndefined();
        mainPanel.setSizeUndefined();
        setSizeUndefined();
    }

    private void generateProblemsSection(Supplier<Map<Integer, List<String>>> problemsSupplier) {
        TreeData<String> treeData = new TreeData<>();
        Tree<String> problemTree = new Tree<>("Available Problems");
        problemTree.setDataProvider(new TreeDataProvider<>(treeData));
        Grid<SelectedProblem> selectedProblemGrid = new Grid<>(SelectedProblem.class);

        selectedProblemGrid.setCaption("Selected Problems");
        selectedProblemGrid.setDataProvider(DataProvider.ofCollection(selectedProblems));
        selectedProblemGrid.setColumnOrder("size", "name");
        selectedProblemGrid.sort("size", SortDirection.ASCENDING);
        selectedProblemGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        selectedProblemGrid.setSizeUndefined();

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
        problemTree.setSizeUndefined();
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
            refreshGrid(selectedProblemGrid);
        });

        selectedProblemGrid.addItemClickListener(itemClick -> {
            SelectedProblem item = itemClick.getItem();
            selectedProblems.remove(item);
            refreshGrid(selectedProblemGrid);
        });

        HorizontalLayout treeGridLayout = new HorizontalLayout();
        treeGridLayout.addComponents(problemTree, selectedProblemGrid);
        treeGridLayout.setComponentAlignment(problemTree, Alignment.TOP_LEFT);
        treeGridLayout.setComponentAlignment(selectedProblemGrid, Alignment.TOP_RIGHT);

        Button addAllProblemsBtn = new Button("Add All");
        addAllProblemsBtn.addClickListener(generateAddAllClickListener(sizeToNameMap, selectedProblemGrid));

        mainLayout.addComponents(treeGridLayout, addAllProblemsBtn);
        mainLayout.setComponentAlignment(addAllProblemsBtn, Alignment.MIDDLE_RIGHT);


//        TwinColSelect<String> problemSelector = new TwinColSelect<>("Select Your Problems");
//        problemSelector.setLeftColumnCaption("Available Problems");
//        problemSelector.setRightColumnCaption("Selected Problems");
//        final List<String> availableProblems = this.service.getAvailableProblems();
//        problemSelector.setDataProvider(DataProvider.ofCollection(availableProblems));
//
//
//        problemSelector.addSelectionListener((MultiSelectionListener<String>) event -> {
//            selectedProblems.clear();
//            selectedProblems.addAll(event.getAllSelectedItems());
//        });
//
//
//
//        _problemsContainer.addComponent(problemSelector);
//        _problemsContainer.setComponentAlignment(problemSelector, Alignment.TOP_CENTER);
//        _problemsContainer.addComponent(addAllProblemsBtn);
//        _problemsContainer.setComponentAlignment(addAllProblemsBtn, Alignment.MIDDLE_RIGHT);
    }

    private void refreshGrid(Grid<SelectedProblem> selectedProblemGrid) {
        selectedProblemGrid.getDataProvider().refreshAll();
        selectedProblemGrid.sort("size", SortDirection.ASCENDING);
    }

    private Button.ClickListener generateAddAllClickListener(Map<Integer, List<String>> map, Grid<SelectedProblem> grid) {
        return (Button.ClickListener) event ->
                map.forEach((size, names) ->
                        names.forEach(name -> {
                            SelectedProblem selected = new SelectedProblem(name, size);
                            selectedProblems.add(selected);
                            refreshGrid(grid);
                        }));
    }
}
