package FinalProject.PL;

import FinalProject.PL.CustomComponents.ProblemSelector;
import FinalProject.PL.UIEntities.ProblemAlgoPair;
import FinalProject.PL.UIEntities.SelectedProblem;
import FinalProject.Service;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.event.selection.MultiSelectionListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.server.Responsive;
import com.vaadin.server.StreamVariable;
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

    private VerticalLayout _algorithmsContainer;

    private Button startExperimentBtn = new Button("Start Experiment");
    private TextField numberOfIterationsTxt = new TextField("Select Number of Iterations");
    private Button addNewAlgorithmBtn = new Button("Add New Algorithm");

    private final List<String> selectedAlgorithms = new ArrayList<>();
    private final Set<SelectedProblem> selectedProblems = new HashSet<>();

    private Service service;
    private ExperimentRunningPresenter experimentRunningPresenter;
    private TwinColSelect<String> algorithmSelector;
    private HorizontalLayout configurationLayout;

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {


    //TODO gal for final iteration prevent use of more than one browser tab
        _algorithmsContainer = new VerticalLayout();
        _algorithmsContainer.setWidth("100%");
        VerticalLayout _problemsContainer = new VerticalLayout();

        this.service = UiHandler.service;
        this.experimentRunningPresenter = UiHandler.experimentRunningPresenter;

        VerticalLayout mainLayout = new VerticalLayout();

        generateAlgorithmsSection();
        ProblemSelector problemSelector = new ProblemSelector(selectedProblems, () -> service.getAvailableProblems());
        problemSelector.addStyleName("with-min-width");
        Responsive.makeResponsive(problemSelector);
        _problemsContainer.addComponent(problemSelector);
        _problemsContainer.setWidth("100%");

        configurationLayout = new HorizontalLayout();
        configurationLayout.addComponent(_problemsContainer);
        configurationLayout.addComponent(_algorithmsContainer);
        configurationLayout.setWidth("100%");

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

        setSizeFull();
    }


    private void generateAlgorithmsSection() {
        algorithmSelector = new TwinColSelect<>();
        algorithmSelector.setLeftColumnCaption("Available Algorithms");
        algorithmSelector.setRightColumnCaption("Selected Algorithms");
        final List<String> availableAlgorithms = refreshAlgorithms();

        algorithmSelector.addSelectionListener((MultiSelectionListener<String>) event -> {
            selectedAlgorithms.clear();
            selectedAlgorithms.addAll(event.getAllSelectedItems());
        });

        Button addAllAlgorithmsBtn = new Button("Add All");
        addAllAlgorithmsBtn.addClickListener(generateAddAllClickListener(availableAlgorithms, algorithmSelector));

        VerticalLayout midLayout = new VerticalLayout();

        midLayout.addComponent(algorithmSelector);
        midLayout.setComponentAlignment(algorithmSelector, Alignment.TOP_CENTER);
        midLayout.addComponent(addAllAlgorithmsBtn);
        midLayout.setComponentAlignment(addAllAlgorithmsBtn, Alignment.MIDDLE_RIGHT);

        Panel algoPanel = new Panel();
        algoPanel.setContent(midLayout);
        algoPanel.setCaption("Select your algorithms");
        _algorithmsContainer.addComponent(algoPanel);

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
        while (componentIterator.hasNext())
        {
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
        if (clickedButton.equals(startExperimentBtn))
        {
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

    @Override
    public void browserWindowResized(Page.BrowserWindowResizeEvent event) {
        if (configurationLayout != null && event.getWidth() < configurationLayout.getWidth()) {
            configurationLayout.setWidth(event.getWidth(), Unit.PIXELS);
        }
    }
}
