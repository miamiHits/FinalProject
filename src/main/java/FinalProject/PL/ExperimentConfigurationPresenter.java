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

public class ExperimentConfigurationPresenter extends Panel implements View, Button.ClickListener, Page.BrowserWindowResizeListener {

    private Button startExperimentBtn = new Button("Start Experiment");
    private TextField numberOfIterationsTxt = new TextField("Select Number of Iterations");
    private Button addNewAlgorithmBtn = new Button("Add New Algorithm");

    private final List<String> selectedAlgorithms = new ArrayList<>();
    private final Set<SelectedProblem> selectedProblems = new HashSet<>();

    private Service service;
    private ExperimentRunningPresenter experimentRunningPresenter;
    private TwinColSelect<String> algorithmSelector;
    private boolean isHorizontal = true;
    private ResponsiveLayout configurationLayout;
    private Grid<SelectedProblem> selectedProblemGrid;
    private Tree<String> problemTree;

    public ExperimentConfigurationPresenter() {
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
    //TODO gal for final iteration prevent use of more than one browser tab
//        _algorithmsContainer = new VerticalLayout();
//        _algorithmsContainer.setWidth("100%");
//        Responsive.makeResponsive(_algorithmsContainer);
//        problemsContainer = new VerticalLayout();
//        Responsive.makeResponsive(problemsContainer);
        this.service = UiHandler.service;
        this.experimentRunningPresenter = UiHandler.experimentRunningPresenter;

        VerticalLayout mainLayout = new VerticalLayout();

        configurationLayout = new ResponsiveLayout();
        ResponsiveRow row = configurationLayout.addRow();
        configurationLayout.setSizeFull();
//        configurationLayout.setSpacing(true);
        generateProblemsSection(row);
        generateAlgorithmsSection();
//        ProblemSelector problemSelector = new ProblemSelector(selectedProblems, () -> service.getAvailableProblems());
//        Responsive.makeResponsive(problemSelector);
//        problemsContainer.addComponent(problemSelector);
//        problemsContainer.setCaption("Select Your Problems");
//        problemsContainer.setWidth("100%");

//        configurationLayout.addComponent(problemsContainer, 0, 0);
//        configurationLayout.addComponent(_algorithmsContainer, 1, 0);
//        _algorithmsContainer.setHeight(problemsContainer.getHeight(), Unit.PIXELS);
//        Responsive.makeResponsive(configurationLayout);
//        configurationLayout.setWidth("100%");
////        currConfigLayout = configurationLayout;

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
        mainLayout.addStyleName("myresponsivelayout");
        Responsive.makeResponsive(mainLayout);

        UI.getCurrent().getPage().addBrowserWindowResizeListener(this);

//        _algorithmsContainer.setHeight(problemsContainer.getHeight(), Unit.PIXELS);

        setSizeFull();
    }


    private void generateAlgorithmsSection() {
        algorithmSelector = new TwinColSelect<>();
        algorithmSelector.setWidth("100%");
        algorithmSelector.setHeight("100%");
        Responsive.makeResponsive(algorithmSelector);
        algorithmSelector.setLeftColumnCaption("Available Algorithms");
        algorithmSelector.setRightColumnCaption("Selected Algorithms");
        algorithmSelector.setCaption("Select your algorithms");
        final List<String> availableAlgorithms = refreshAlgorithms();

        algorithmSelector.addSelectionListener((MultiSelectionListener<String>) event -> {
            selectedAlgorithms.clear();
            selectedAlgorithms.addAll(event.getAllSelectedItems());
        });
        configurationLayout.addComponent(algorithmSelector, 2, 0);

        Button addAllAlgorithmsBtn = new Button("Add All");
        addAllAlgorithmsBtn.addClickListener(generateAddAllClickListener(availableAlgorithms, algorithmSelector));

        configurationLayout.addComponent(addAllAlgorithmsBtn, 2, 1);
        configurationLayout.setComponentAlignment(addAllAlgorithmsBtn, Alignment.BOTTOM_RIGHT);
//        _algorithmsContainer.addComponent(algorithmSelector);
//        _algorithmsContainer.setComponentAlignment(algorithmSelector, Alignment.TOP_CENTER);
//        _algorithmsContainer.addComponent(addAllAlgorithmsBtn);
//        _algorithmsContainer.setComponentAlignment(addAllAlgorithmsBtn, Alignment.MIDDLE_RIGHT);
//
//        _algorithmsContainer.setCaption("Select your algorithms");
//        Panel algoPanel = new Panel();
//        algoPanel.setContent(midLayout);
//        algoPanel.setCaption("Select your algorithms");
//        _algorithmsContainer.addComponent(algoPanel);

    }

    private void generateProblemsSection(ResponsiveRow row) {
        problemTree = new Tree<>("Available Problems");
        problemTree.addStyleNames("with-min-width", "with-max-width");
        Responsive.makeResponsive(problemTree);
        selectedProblemGrid = new Grid<>(SelectedProblem.class);
        selectedProblemGrid.addStyleName("problem-grid-style");
        selectedProblemGrid.setSizeUndefined();
        Responsive.makeResponsive(selectedProblemGrid);

        Map<Integer, List<String>> sizeToNameMap = initTree(problemTree);
        initGrid();

        Button addAllProblemsBtn = new Button("Add All");
        addAllProblemsBtn.addClickListener(generateAddAllClickListener(sizeToNameMap, selectedProblemGrid));

        ResponsiveColumn problemCol =  row.addColumn();
        ResponsiveRow topRow = new ResponsiveRow();
        topRow.addComponents(problemTree, selectedProblemGrid);
        topRow.setSpacing(true);
        problemCol.setComponent(topRow);
        row.addColumn(problemCol);
//        configurationLayout.addComponent(problemTree, 0, 0);
//        configurationLayout.addComponent(selectedProblemGrid, 1, 0);
//        HorizontalLayout treeGridLayout = new HorizontalLayout();
//        treeGridLayout.setWidth("100%");
//        treeGridLayout.setSpacing(true);
//        treeGridLayout.addComponent(problemTree);
//        treeGridLayout.setComponentAlignment(problemTree, Alignment.TOP_LEFT);
//        treeGridLayout.addComponent(selectedProblemGrid);
//        treeGridLayout.setComponentAlignment(selectedProblemGrid, Alignment.TOP_RIGHT);

//        mainLayout.addComponents(treeGridLayout);
//        mainLayout.setComponentAlignment(treeGridLayout, Alignment.TOP_CENTER);

//        configurationLayout.addComponent(addAllProblemsBtn, 1, 1);
//        configurationLayout.setComponentAlignment(addAllProblemsBtn, Alignment.BOTTOM_RIGHT);
//        mainLayout.addComponent(addAllProblemsBtn);
//        mainLayout.setComponentAlignment(addAllProblemsBtn, Alignment.BOTTOM_RIGHT);
//        Responsive.makeResponsive(mainLayout);
//        mainLayout.setWidth("100%");
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

    private Button.ClickListener generateAddAllClickListener(Map<Integer, List<String>> map, Grid<SelectedProblem> grid) {
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

//    private void generateProblemsSection() {
//        TreeData<String> treeData = new TreeData<>();
//        Tree<String> problemTree = new Tree<>("Available Problems");
//        problemTree.setDataProvider(new TreeDataProvider<>(treeData));
//        Grid<SelectedProblem> selectedProblemGrid = new Grid<>(SelectedProblem.class);
//
//        selectedProblemGrid.setCaption("Selected Problems");
//        selectedProblemGrid.setDataProvider(DataProvider.ofCollection(selectedProblems));
//        selectedProblemGrid.setColumnOrder("size", "name");
//        selectedProblemGrid.sort("size", SortDirection.ASCENDING);
//        selectedProblemGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
//        selectedProblemGrid.setSizeUndefined();
//
//        Map<Integer, List<String>> sizeToNameMap = service.getAvailableProblems();
//        sizeToNameMap.entrySet().stream()
//                .sorted(Comparator.comparingInt(Map.Entry::getKey))
//                .forEach(entry -> {
//                    String size = String.valueOf(entry.getKey());
//                    List<String> names = entry.getValue();
//                    treeData.addRootItems(size);
//                    treeData.addItems(size, names);
//                    problemTree.collapse();
//                });
//        problemTree.setContentMode(ContentMode.TEXT);
//        problemTree.setSizeUndefined();
//        problemTree.addItemClickListener(itemClick -> {
//
//            String item = itemClick.getItem();
//            List<String> children = treeData.getChildren(item);
//            //is a specific problem
//            if (children == null || children.size() == 0) {
//                int parent = Integer.parseInt(treeData.getParent(item));
//                SelectedProblem selected = new SelectedProblem(item, parent);
//                selectedProblems.add(selected);
//            }
//            //is a folder
//            else {
//                List<SelectedProblem> toAdd = children.stream()
//                        .map(child -> new SelectedProblem(child, Integer.parseInt(item)))
//                        .collect(Collectors.toList());
//                selectedProblems.addAll(toAdd);
//            }
//            refreshGrid(selectedProblemGrid);
//        });
//
//        selectedProblemGrid.addItemClickListener(itemClick -> {
//            SelectedProblem item = itemClick.getItem();
//            selectedProblems.remove(item);
//            refreshGrid(selectedProblemGrid);
//        });
//
//        HorizontalLayout treeGridLayout = new HorizontalLayout();
//        treeGridLayout.addComponents(problemTree, selectedProblemGrid);
//        treeGridLayout.setCaption("Select Your Problems");
//        treeGridLayout.setComponentAlignment(problemTree, Alignment.TOP_LEFT);
//        treeGridLayout.setComponentAlignment(selectedProblemGrid, Alignment.TOP_RIGHT);
//
//        Button addAllProblemsBtn = new Button("Add All");
//        addAllProblemsBtn.addClickListener(generateAddAllClickListener(sizeToNameMap, selectedProblemGrid));
//
//        _problemsContainer.addComponents(treeGridLayout, addAllProblemsBtn);
//        _problemsContainer.setComponentAlignment(addAllProblemsBtn, Alignment.MIDDLE_RIGHT);
//
//
////        TwinColSelect<String> problemSelector = new TwinColSelect<>("Select Your Problems");
////        problemSelector.setLeftColumnCaption("Available Problems");
////        problemSelector.setRightColumnCaption("Selected Problems");
////        final List<String> availableProblems = this.service.getAvailableProblems();
////        problemSelector.setDataProvider(DataProvider.ofCollection(availableProblems));
////
////
////        problemSelector.addSelectionListener((MultiSelectionListener<String>) event -> {
////            selectedProblems.clear();
////            selectedProblems.addAll(event.getAllSelectedItems());
////        });
////
////
////
////        _problemsContainer.addComponent(problemSelector);
////        _problemsContainer.setComponentAlignment(problemSelector, Alignment.TOP_CENTER);
////        _problemsContainer.addComponent(addAllProblemsBtn);
////        _problemsContainer.setComponentAlignment(addAllProblemsBtn, Alignment.MIDDLE_RIGHT);
//    }

//    private void refreshGrid(Grid<SelectedProblem> selectedProblemGrid) {
//        selectedProblemGrid.getDataProvider().refreshAll();
//        selectedProblemGrid.sort("size", SortDirection.ASCENDING);
//    }

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

//    private Button.ClickListener generateAddAllClickListener(Map<Integer, List<String>> map, Grid<SelectedProblem> grid) {
//        return (Button.ClickListener) event ->
//                map.forEach((size, names) ->
//                        names.forEach(name -> {
//                        SelectedProblem selected = new SelectedProblem(name, size);
//                        selectedProblems.add(selected);
//                            refreshGrid(grid);
//                        }));
//    }


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

//    private void horizontalToVerticalToggle() {
//        Layout layout;
//        if (isHorizontal) {
//            layout = new VerticalLayout();
//            isHorizontal = false;
//        } else {
//            layout = new HorizontalLayout();
//            isHorizontal = true;
//        }
//        Layout parent = (Layout) currConfigLayout.getParent();
//        layout.setCaption(currConfigLayout.getCaption());
//        List<Component> components = new ArrayList<>();
//        currConfigLayout.forEach(components::add);
////        parent.removeComponent(currConfigLayout);
//        components.forEach(layout::addComponent);
//        Layout old = currConfigLayout;
//        currConfigLayout = layout;
//        parent.replaceComponent(old, layout);
//    }

//    private float getConfigConatinerWidth() {
//        float problemsWidth = problemsContainer.getWidth();
//        float algorithmWidth = algorithmSelector.getWidth();
//        if (isHorizontal) {
//            return problemsWidth + algorithmWidth;
//        }
//        else {
//            return Math.max(problemsWidth, algorithmWidth);
//        }
//    }

    @Override
    public void browserWindowResized(Page.BrowserWindowResizeEvent event) {
//        if (currConfigLayout != null && event.getWidth() < currConfigLayout.getWidth()) {
//            currConfigLayout.setWidth(event.getWidth(), Unit.PIXELS);
//            if (getConfigConatinerWidth() < event.getWidth()) {
//                horizontalToVerticalToggle();
//            }
//        }
//        _algorithmsContainer.setHeight(problemsContainer.getHeight(), Unit.PIXELS);
    }
}
