package FinalProject.PL;

import FinalProject.BL.AlgoAddResult;
import FinalProject.Config;
import FinalProject.PL.UIEntities.ProblemAlgoPair;
import FinalProject.PL.UIEntities.SelectedProblem;
import FinalProject.Service;
import com.jarektoro.responsivelayout.ResponsiveColumn;
import com.jarektoro.responsivelayout.ResponsiveLayout;
import com.jarektoro.responsivelayout.ResponsiveRow;
import com.vaadin.data.HasValue;
import com.vaadin.data.TreeData;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.data.provider.TreeDataProvider;
import com.vaadin.event.selection.MultiSelectionListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.server.StreamVariable;
import com.vaadin.server.UserError;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.dnd.FileDropTarget;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public class ExperimentConfigurationPresenter extends Panel implements View, Button.ClickListener, HasValue.ValueChangeListener<String> {

    private Button startExperimentBtn = new Button("Start Experiment");
    private TextField numberOfIterationsTxt = new TextField("Select Number of Iterations");
    private int numberOfIterations = 0;
    private Button addNewAlgorithmBtn = new Button("Add New Algorithm");

    private final List<String> selectedAlgorithms = new ArrayList<>();
    private final Set<SelectedProblem> selectedProblems = new HashSet<>();

    private Service service;
    private ExperimentRunningPresenter experimentRunningPresenter;
    private TwinColSelect<String> algorithmSelector;
    private Grid<SelectedProblem> selectedProblemGrid;

    private final int COL_SIZE = 6;

    private static final Logger logger = Logger.getLogger(ExperimentConfigurationPresenter.class);

    public ExperimentConfigurationPresenter() {
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        logger.debug("enter");
        //TODO gal for final iteration prevent use of more than one browser tab
        this.service = UiHandler.service;
        this.experimentRunningPresenter = UiHandler.experimentRunningPresenter;

        selectedProblems.clear();
        VerticalLayout mainLayout = new VerticalLayout();
        //TODO uncomment to add background image!
//        mainLayout.setStyleName("with-bg-image");

        ResponsiveLayout configurationLayout = new ResponsiveLayout(ResponsiveLayout.ContainerType.FLUID)
                .withSpacing();
        configurationLayout.setSizeFull();
        ResponsiveRow row = configurationLayout.addRow()
                .withAlignment(Alignment.MIDDLE_CENTER)
                .withSpacing(true);
        row.setDefaultRules(2 * COL_SIZE + 1, 2 * COL_SIZE + 1,
                2 * COL_SIZE + 1, 2 * COL_SIZE + 1);

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
        numberOfIterationsTxt.addValueChangeListener(this);

        mainLayout.addComponent(numberOfIterationsTxt);
        mainLayout.addComponent(addNewAlgorithmBtn);
        mainLayout.addComponent(startExperimentBtn);
        setAlignemntToAllComponents(mainLayout, Alignment.MIDDLE_CENTER);
        mainLayout.addStyleName("conf-main-layout");

        setContent(mainLayout);

        setSizeFull();
    }


    private void generateAlgorithmsSection(ResponsiveRow row) {
        algorithmSelector = new TwinColSelect<>();
        algorithmSelector.setLeftColumnCaption("Available Algorithms");
        algorithmSelector.setRightColumnCaption("Selected Algorithms");
        algorithmSelector.setCaption("Select your algorithms");
        algorithmSelector.addStyleName("algo-selector");
        final List<String> availableAlgorithms = refreshAlgorithms();

        algorithmSelector.addSelectionListener((MultiSelectionListener<String>) event -> {
            selectedAlgorithms.clear();
            selectedAlgorithms.addAll(event.getAllSelectedItems());
        });

        Button addAllAlgorithmsBtn = new Button("Add All");
        addAllAlgorithmsBtn.addClickListener((Button.ClickListener) event ->
        {
            logger.debug("add all clicked");
            List<String> algorithms = refreshAlgorithms();
            for (String item : algorithms) {
                algorithmSelector.select(item);
            }
        });

        ResponsiveRow topRow = new ResponsiveRow()
                .withComponents(algorithmSelector)
                .withAlignment(Alignment.TOP_RIGHT);
        topRow.setDefaultRules(COL_SIZE, COL_SIZE, COL_SIZE, COL_SIZE);
        ResponsiveRow bottomRow = new ResponsiveRow()
                .withComponents(addAllAlgorithmsBtn)
                .withSpacing(true)
                .withAlignment(Alignment.BOTTOM_LEFT);
        bottomRow.setDefaultRules(COL_SIZE, COL_SIZE, COL_SIZE, COL_SIZE);
        ResponsiveLayout algoLayout = new ResponsiveLayout()
                .withSpacing();
        algoLayout.addRow(topRow);
        algoLayout.addRow(bottomRow);
        algoLayout.setHeight("100%");
        ResponsiveColumn col = row.addColumn()
                .withComponent(algoLayout);
        col.setAlignment(ResponsiveColumn.ColumnComponentAlignment.RIGHT);
    }

    private void generateProblemsSection(ResponsiveRow row) {
        Tree<String> problemTree = new Tree<>("Available Problems");
        problemTree.addStyleNames("with-min-width", "with-max-width");
        selectedProblemGrid = new Grid<>(SelectedProblem.class);
        selectedProblemGrid.addStyleName("problem-grid-style");
        selectedProblemGrid.getColumn("size").setMaximumWidth(100);

        Map<Integer, List<String>> sizeToNameMap = initTree(problemTree);
        initGrid();

        Button addAllProblemsBtn = new Button("Add All");
        addAllProblemsBtn.addClickListener( event ->
        {
            logger.debug("add all clicked");
            sizeToNameMap.forEach((size, names) ->
                    names.forEach(name -> {
                        SelectedProblem selected = new SelectedProblem(name, size);
                        selectedProblems.add(selected);
                        refreshGrid();
                    }));
        });

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.addComponents(problemTree, selectedProblemGrid);
        horizontalLayout.setComponentAlignment(problemTree, Alignment.TOP_LEFT);
        horizontalLayout.setComponentAlignment(selectedProblemGrid, Alignment.TOP_RIGHT);
        horizontalLayout.setSizeFull();
        ResponsiveRow topRow = new ResponsiveRow()
                .withComponents(horizontalLayout)
                .withAlignment(Alignment.TOP_LEFT)
                .withDefaultRules(COL_SIZE, COL_SIZE, COL_SIZE, COL_SIZE);
        topRow.setDefaultComponentAlignment(Alignment.TOP_LEFT);
        ResponsiveRow bottomRow = new ResponsiveRow()
                .withComponents(addAllProblemsBtn)
                .withAlignment(Alignment.BOTTOM_LEFT)
                .withDefaultRules(COL_SIZE, COL_SIZE, COL_SIZE, COL_SIZE);

        ResponsiveLayout problemsLayout = new ResponsiveLayout()
                .withCaption("Select your problems");
        problemsLayout.addRow(topRow);
        problemsLayout.addRow(bottomRow);

        ResponsiveColumn problemCol = new ResponsiveColumn()
                .withDisplayRules(COL_SIZE, COL_SIZE, COL_SIZE, COL_SIZE)
                .withComponent(problemsLayout);
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
        logger.debug("refreshing problems grid");
        this.selectedProblemGrid.getDataProvider().refreshAll();
        this.selectedProblemGrid.sort("size", SortDirection.ASCENDING);
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

    @Override
    public void buttonClick(Button.ClickEvent event) {
        Button clickedButton = event.getButton();
        if (clickedButton.equals(startExperimentBtn)) {
            startExperimentClicked();
        } else if (clickedButton.equals(addNewAlgorithmBtn)) {
            addNewAlgoClicked();
        }
    }

    private void addNewAlgoClicked() {
        Window algoAddPopup = new Window("Drop your file here!");
        Label dropArea = new Label("Drop your algorithm file here");
        dropArea.setSizeUndefined();
        dropArea.addStyleNames(ValoTheme.LABEL_HUGE, ValoTheme.LABEL_BOLD);

        final String COMPILED_ALGO_DIR = Config.getStringPropery(Config.EMBEDDED_ALGO_DIR);
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
                    try {
                        fileOutputStream = new FileOutputStream(path);
                        return fileOutputStream;
                    } catch (FileNotFoundException e) {
                        logger.warn("Cannot find file " + path);
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
                    AlgoAddResult result = service.addNewAlgo(COMPILED_ALGO_DIR, fileName);
                    if (result.isSuccess()) {
                        refreshAlgorithms();
                        Notification.show(fileName + " was added successfully!");
                    } else {
                        new Notification("Failed!", "Reason:\n" + result.getErrorMsg(), Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());
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

    private void startExperimentClicked() {

        UserError algorithmSelectorError =
                selectedAlgorithms.isEmpty() ?
                        new UserError("Please Select at Least One Algorithm") :
                        null;
        algorithmSelector.setComponentError(algorithmSelectorError);

        UserError problemSelectorError =
                selectedProblems.isEmpty() ?
                        new UserError("Please Select at Least One Problem") :
                        null;
        selectedProblemGrid.setComponentError(problemSelectorError);

        UserError numberIterationError =
                numberOfIterationsTxt.isEmpty() ?
                        new UserError("Please Provide the Required Number of Iterations") :
                        null;
        numberOfIterationsTxt.setComponentError(numberIterationError);


        if (numberOfIterations > 0 &&
                !selectedAlgorithms.isEmpty() &&
                !selectedProblems.isEmpty())
        {
            setExperimentRunningPairs();

            service.setAlgorithmsToExperiment(selectedAlgorithms, numberOfIterations);
            service.setProblemsToExperiment(selectedProblems.stream().map(SelectedProblem::getName).collect(Collectors.toList()));
            service.runExperiment();
            selectedAlgorithms.clear();
            algorithmSelector.clear();
            numberOfIterationsTxt.clear();
            numberOfIterationsTxt.setComponentError(null);


            getUI().access(() -> {
                getUI().getNavigator().navigateTo(UiHandler.EXPERIMENT_RUNNING);
            });

        }
    }

    private void setExperimentRunningPairs () {
        List<ProblemAlgoPair> problemAlgoPairs = new ArrayList<>(selectedAlgorithms.size() * selectedProblems.size());
        selectedAlgorithms.forEach(algo -> {
            selectedProblems.forEach(problem -> problemAlgoPairs.add(new ProblemAlgoPair(algo, problem.getName())));
        });

        experimentRunningPresenter.setAlgorithmProblemPairs(problemAlgoPairs);
    }


    @Override
    public void valueChange(HasValue.ValueChangeEvent<String> event) {
        try
        {
            int result = Integer.valueOf(numberOfIterationsTxt.getValue());
            if (result <= 0)
            {
                throw new InvalidParameterException();
            }
            this.numberOfIterations = result;
            this.numberOfIterationsTxt.setComponentError(null);
        }
        catch(NumberFormatException e)
        {
            numberOfIterationsTxt.setComponentError(new UserError("This Must Be a Number"));
            this.numberOfIterations = 0;
        }
        catch (InvalidParameterException e)
        {
            numberOfIterationsTxt.setComponentError(new UserError("Number of Iterations Must Be Greater than 0"));
            this.numberOfIterations = 0;
        }

    }
}

