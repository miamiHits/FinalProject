package FinalProject.PL;

import FinalProject.PL.UIEntities.ProblemAlgoPair;
import FinalProject.PL.UIEntities.SelectedProblem;
import FinalProject.Service;
import com.jarektoro.responsivelayout.ResponsiveColumn;
import com.jarektoro.responsivelayout.ResponsiveLayout;
import com.jarektoro.responsivelayout.ResponsiveRow;
import com.vaadin.data.TreeData;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.data.provider.TreeDataProvider;
import com.vaadin.event.selection.MultiSelectionListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.server.Responsive;
import com.vaadin.server.StreamVariable;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.dnd.FileDropTarget;
import com.vaadin.ui.themes.ValoTheme;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public class ExperimentConfigurationPresenter extends Panel implements View, Button.ClickListener {

    private Button startExperimentBtn = new Button("Start Experiment");
    private TextField numberOfIterationsTxt = new TextField("Select Number of Iterations");
    private Button addNewAlgorithmBtn = new Button("Add New Algorithm");

    private final List<String> selectedAlgorithms = new ArrayList<>();
    private final Set<SelectedProblem> selectedProblems = new HashSet<>();

    private Service service;
    private ExperimentRunningPresenter experimentRunningPresenter;
    private TwinColSelect<String> algorithmSelector;
    private Grid<SelectedProblem> selectedProblemGrid;

    private final int COL_SIZE = 6;

    public ExperimentConfigurationPresenter() {
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
    //TODO gal for final iteration prevent use of more than one browser tab
        this.service = UiHandler.service;
        this.experimentRunningPresenter = UiHandler.experimentRunningPresenter;

        VerticalLayout mainLayout = new VerticalLayout();

        ResponsiveLayout configurationLayout = new ResponsiveLayout(ResponsiveLayout.ContainerType.FLUID)
//                .withFullSize()
//                .withFlexible()
                .withSpacing();
        configurationLayout.setSizeFull();
        ResponsiveRow row = configurationLayout.addRow()
                .withAlignment(Alignment.MIDDLE_CENTER)
//                .withGrow(true)
//                .withShrink(true)
                .withMargin(true);
        row.setDefaultRules(2 * COL_SIZE + 1, 2 * COL_SIZE + 1, 2 * COL_SIZE + 1, 2 * COL_SIZE + 1);
//        row.setSizeFull();

        generateProblemsSection(row);
        generateAlgorithmsSection(row);

        Label mainTitleLbl = new Label("SHAS");
        mainTitleLbl.addStyleName("v-label-h1");
        mainTitleLbl.addStyleName("conf-title");


        Label subtitleLbl = new Label("Smart Home Agent Simulator");
        subtitleLbl.addStyleName(ValoTheme.LABEL_LARGE);

        mainLayout.addComponent(mainTitleLbl);
        mainLayout.addComponent(subtitleLbl);

        mainLayout.addComponent(configurationLayout);

        startExperimentBtn.addClickListener(this);
        startExperimentBtn.addStyleName("conf-start-btn");
        addNewAlgorithmBtn.addClickListener(this);

        mainLayout.addComponent(numberOfIterationsTxt);
        mainLayout.addComponent(addNewAlgorithmBtn);
        mainLayout.addComponent(startExperimentBtn);
        setAlignemntToAllComponents(mainLayout, Alignment.MIDDLE_CENTER);
        mainLayout.addStyleName("conf-main-layout");

        setContent(mainLayout);
//        mainLayout.addStyleName("myresponsivelayout");
//        Responsive.makeResponsive(mainLayout);

        setSizeFull();
    }


    private void generateAlgorithmsSection(ResponsiveRow row) {
        algorithmSelector = new TwinColSelect<>();
//        algorithmSelector.setSizeFull();
//        Responsive.makeResponsive(algorithmSelector);
        algorithmSelector.setLeftColumnCaption("Available Algorithms");
        algorithmSelector.setRightColumnCaption("Selected Algorithms");
        algorithmSelector.setCaption("Select your algorithms");
        final List<String> availableAlgorithms = refreshAlgorithms();

        algorithmSelector.addSelectionListener((MultiSelectionListener<String>) event -> {
            selectedAlgorithms.clear();
            selectedAlgorithms.addAll(event.getAllSelectedItems());
        });

        Button addAllAlgorithmsBtn = new Button("Add All");
        addAllAlgorithmsBtn.addClickListener(generateAddAllClickListener(availableAlgorithms, algorithmSelector));

        ResponsiveRow topRow = new ResponsiveRow()
                .withComponents(algorithmSelector)
                .withSpacing(true)
                .withAlignment(Alignment.TOP_RIGHT)
                .withMargin(true);
//                .withShrink(true)
//                .withGrow(true);
        topRow.setDefaultRules(COL_SIZE,COL_SIZE,COL_SIZE,COL_SIZE);
        ResponsiveRow bottomRow = new ResponsiveRow()
                .withComponents(addAllAlgorithmsBtn)
                .withSpacing(true)
                .withAlignment(Alignment.BOTTOM_LEFT);
//                .withShrink(true)
//                .withGrow(true);
        bottomRow.setDefaultRules(COL_SIZE,COL_SIZE,COL_SIZE,COL_SIZE);
        ResponsiveLayout algoLayout = new ResponsiveLayout()
//                .withFullSize()
                .withSpacing();
        algoLayout.addRow(topRow);
        algoLayout.addRow(bottomRow);
        ResponsiveColumn col = row.addColumn()
//                .withGrow(true)
//                .withShrink(true)
                .withComponent(algoLayout);
//        col.setSizeFull();
        col.setAlignment(ResponsiveColumn.ColumnComponentAlignment.RIGHT);
    }

    private void generateProblemsSection(ResponsiveRow row) {
        Tree<String> problemTree = new Tree<>("Available Problems");
//        problemTree.setSizeFull();
//        Responsive.makeResponsive(problemTree);
        problemTree.addStyleNames("with-min-width", "with-max-width");
        selectedProblemGrid = new Grid<>(SelectedProblem.class);
        selectedProblemGrid.addStyleName("problem-grid-style");
//        selectedProblemGrid.setSizeFull();
//        Responsive.makeResponsive(selectedProblemGrid);

        Map<Integer, List<String>> sizeToNameMap = initTree(problemTree);
        initGrid();

        Button addAllProblemsBtn = new Button("Add All");
        addAllProblemsBtn.addClickListener(generateAddAllClickListener(sizeToNameMap));

        ResponsiveRow topRow = new ResponsiveRow()
                .withComponents(problemTree, selectedProblemGrid)
                .withAlignment(Alignment.TOP_LEFT)
//                .withGrow(true)
//                .withShrink(true)
                .withMargin(true)
                .withDefaultRules(COL_SIZE,COL_SIZE,COL_SIZE,COL_SIZE);
//        topRow.setSizeUndefined();
        topRow.setDefaultComponentAlignment(Alignment.TOP_LEFT);
        ResponsiveRow bottomRow = new ResponsiveRow()
                .withComponents(addAllProblemsBtn)
                .withAlignment(Alignment.BOTTOM_LEFT)
//                .withGrow(true)
//                .withShrink(true)
                .withMargin(true)
                .withDefaultRules(COL_SIZE,COL_SIZE,COL_SIZE,COL_SIZE);
//        bottomRow.setSizeUndefined();

        ResponsiveLayout problemsLayout = new ResponsiveLayout();
        problemsLayout.addRow(topRow);
        problemsLayout.addRow(bottomRow);
        problemsLayout.setSpacing();

        ResponsiveColumn problemCol =  new ResponsiveColumn()
                .withDisplayRules(COL_SIZE,COL_SIZE,COL_SIZE,COL_SIZE)
                .withComponent(problemsLayout);
//                .withGrow(true)
//                .withShrink(true);
        row.addColumn(problemCol);
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

    private Map<Integer, List<String>> initTree(Tree<String> problemTree) {
        TreeData<String> treeData = new TreeData<>();
        problemTree.setDataProvider(new TreeDataProvider<>(treeData));
        Map<Integer, List<String>> sizeToNameMap = service.getAvailableProblems();
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

    private Button.ClickListener generateAddAllClickListener(Map<Integer, List<String>> map) {
        return (Button.ClickListener) event ->
                map.forEach((size, names) ->
                        names.forEach(name -> {
                            SelectedProblem selected = new SelectedProblem(name, size);
                            selectedProblems.add(selected);
                            refreshGrid();
                        }));
    }

    private List<String> refreshAlgorithms() {
        final List<String> availableAlgorithms = this.service.getAvailableAlgorithms();
        ListDataProvider<String> dataProvider = DataProvider.ofCollection(availableAlgorithms);
        algorithmSelector.setDataProvider(dataProvider);
        return availableAlgorithms;
    }

    public static void setAlignemntToAllComponents(AbstractOrderedLayout layout, Alignment alignment) {
        Iterator<Component> componentIterator = layout.iterator();
        while (componentIterator.hasNext()) {
            Component currentComponent = componentIterator.next();
            layout.setComponentAlignment(currentComponent, alignment);
        }
    }

    private Button.ClickListener generateAddAllClickListener(List<String> items, AbstractMultiSelect<String> component) {
        return (Button.ClickListener) event -> {
            for (String item : items) {
                component.select(item);
            }
        };
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        Button clickedButton = event.getButton();
        if (clickedButton.equals(startExperimentBtn)) {
            startExperimentClicked();
        }
        else if (clickedButton.equals(addNewAlgorithmBtn)) {
            addNewAlgoClicked();
        }
    }

    private void addNewAlgoClicked() {
        Window algoAddPopup = new Window("Drop your file here!");
        Label dropArea = new Label("Drop your algorithm file here");
        dropArea.setSizeUndefined();
        dropArea.addStyleNames(ValoTheme.LABEL_HUGE, ValoTheme.LABEL_BOLD);

        final String COMPILED_ALGO_DIR = "target/classes/FinalProject/BL/Agents";
        VerticalLayout layout = new VerticalLayout(dropArea);
        layout.addStyleNames(ValoTheme.DRAG_AND_DROP_WRAPPER_NO_HORIZONTAL_DRAG_HINTS, ValoTheme.LAYOUT_WELL);
        layout.setComponentAlignment(dropArea, Alignment.MIDDLE_CENTER);
        new FileDropTarget<>(layout, event -> {
            Collection<Html5File> files = event.getFiles();
            files.forEach(file -> file.setStreamVariable(new StreamVariable() {

                FileOutputStream fileOutputStream = null;

                // Output stream to write the file to
                @Override
                public OutputStream getOutputStream() {
                    String path = (COMPILED_ALGO_DIR + "/" + file.getFileName())
                            .replaceAll("/", Matcher.quoteReplacement(Matcher.quoteReplacement(File.separator)));
                    try{
                        fileOutputStream = new FileOutputStream(path);
                        return fileOutputStream;
                    }catch (FileNotFoundException e) {
                        Notification.show("Cannot find file " + path);
                    }
                    return null;
                }

                // Returns whether onProgress() is called during upload
                @Override
                public boolean listenProgress() {
                    return false;
                }

                // Called periodically during upload
                @Override
                public void onProgress(StreamingProgressEvent event) {
//                        Notification.show("Progress, bytesReceived="
//                                + event.getBytesReceived());
                    //nothing to do here
                }

                // Called when upload started
                @Override
                public void streamingStarted(StreamingStartEvent event) {
//                    Notification.show("Stream started, fileName=" + event.getFileName());
                }

                // Called when upload finished
                @Override
                public void streamingFinished(StreamingEndEvent event) {
                    Notification.show("upload finished");
                    algoUploadFinished(event.getFileName());
                    algoAddPopup.close();
                }

                // Called when upload failed
                @Override
                public void streamingFailed(StreamingErrorEvent event) {
                    new Notification("Upload failed!", "Could not upload file " + event.getFileName(),
                            Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());
                }
                @Override
                public boolean isInterrupted() {
                    return false;
                }

                private void algoUploadFinished(String fileName) {
                    String result = service.addNewAlgo(COMPILED_ALGO_DIR, fileName);
                    if (result.equalsIgnoreCase("success")) {
                        refreshAlgorithms();
                        Notification.show(fileName + " was added successfully!");
                    }
                    else {
                        new Notification("Failed!", result, Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());
                    }
                }
            }));
        });

        algoAddPopup.setContent(layout);
        algoAddPopup.setResizable(true);
        algoAddPopup.setSizeUndefined();
        algoAddPopup.setClosable(true);
        algoAddPopup.center();
        algoAddPopup.setVisible(true);
        UI.getCurrent().addWindow(algoAddPopup);
    }

    private void startExperimentClicked()
    {

        int numberOfIterations = parseIterationNumber();

        setExperimentRunningPairs();

        service.setAlgorithmsToExperiment(selectedAlgorithms, numberOfIterations);
        service.setProblemsToExperiment(selectedProblems.stream().map(SelectedProblem::getName).collect(Collectors.toList()));
        service.runExperiment();

        getUI().access(() -> {
            getUI().getNavigator().navigateTo(UiHandler.EXPERIMENT_RUNNING);
        });

    }

    private int parseIterationNumber()
    {
        int result = -1;
        try
        {
            result = Integer.valueOf(numberOfIterationsTxt.getValue());
            if (result <= 0)
            {
                throw new NumberFormatException();
            }
        }
        catch(NumberFormatException e)
        {
            //TODO gal implement error message
        }
        numberOfIterationsTxt.clear();
        return result;
    }

    private void setExperimentRunningPairs() {
        List<ProblemAlgoPair> problemAlgoPairs = new ArrayList<>(selectedAlgorithms.size() * selectedProblems.size());
        selectedAlgorithms.forEach(algo -> {
            selectedProblems.forEach(problem -> problemAlgoPairs.add(new ProblemAlgoPair(algo, problem.getName())));
        });

        experimentRunningPresenter.setAlgorithmProblemPairs(problemAlgoPairs);
    }

}
