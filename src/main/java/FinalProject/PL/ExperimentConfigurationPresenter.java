package FinalProject.PL;

import FinalProject.PL.UIEntities.ProblemAlgoPair;
import FinalProject.Service;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.event.selection.MultiSelectionEvent;
import com.vaadin.event.selection.MultiSelectionListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.StreamVariable;
import com.vaadin.ui.*;
import com.vaadin.ui.dnd.FileDropTarget;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;

public class ExperimentConfigurationPresenter extends Panel implements View, Button.ClickListener {

    private VerticalLayout _algorithmsContainer;
    private VerticalLayout _problemsContainer;

    private Button startExperimentBtn = new Button("Start Experiment");
    private TextField numberOfIterationsTxt = new TextField("Select Number of Iterations");
    private Button addNewAlgorithmBtn = new Button("Add New Algorithm");

    private final List<String> selectedAlgorithms = new ArrayList<>();
    private final List<String> selectedProblems = new ArrayList<>();

    private Service service;
    private ExperimentRunningPresenter experimentRunningPresenter;
    private TwinColSelect<String> algorithmSelector;

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {


    //TODO gal for final iteration prevent use of more than one browser tab
        _algorithmsContainer = new VerticalLayout();
        _problemsContainer = new VerticalLayout();

        this.service = UiHandler.service;
        this.experimentRunningPresenter = UiHandler.experimentRunningPresenter;

        VerticalLayout mainLayout = new VerticalLayout();

        generateAlgorithmsSection();
        generateProblemsSection();

        HorizontalLayout configurationLayout = new HorizontalLayout();
        configurationLayout.addComponent(_problemsContainer);
        configurationLayout.addComponent(_algorithmsContainer);

        Label mainTitleLbl = new Label("SHAS");
        mainTitleLbl.addStyleName("v-label-h1");
        mainTitleLbl.addStyleName("conf-title");


        Label subtitleLbl = new Label("Smart Home Agent Simulator");
        subtitleLbl.addStyleName("v-label-h2");
        subtitleLbl.addStyleName("conf-subtitle");

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

        setSizeFull();
    }


    private void generateAlgorithmsSection()
    {
        algorithmSelector = new TwinColSelect<>("Select Your Algorithms");
        algorithmSelector.setLeftColumnCaption("Available Algorithms");
        algorithmSelector.setRightColumnCaption("Selected Algorithms");
        final List<String> availableAlgorithms = refreshAlgorithms();

        algorithmSelector.addSelectionListener((MultiSelectionListener<String>) event -> {
            selectedAlgorithms.clear();
            selectedAlgorithms.addAll(event.getAllSelectedItems());
        });

        Button addAllAlgorithmsBtn = new Button("Add All");
        addAllAlgorithmsBtn.addClickListener(generateAddAllClickListener(availableAlgorithms, algorithmSelector));

        _algorithmsContainer.addComponent(algorithmSelector);
        _algorithmsContainer.setComponentAlignment(algorithmSelector, Alignment.TOP_CENTER);
        _algorithmsContainer.addComponent(addAllAlgorithmsBtn);
        _algorithmsContainer.setComponentAlignment(addAllAlgorithmsBtn, Alignment.MIDDLE_RIGHT);

    }

    private List<String> refreshAlgorithms() {
        final List<String> availableAlgorithms = this.service.getAvailableAlgorithms();
        ListDataProvider<String> dataProvider = DataProvider.ofCollection(availableAlgorithms);
        algorithmSelector.setDataProvider(dataProvider);
        return availableAlgorithms;
    }

    private void generateProblemsSection()
    {
        TwinColSelect<String> problemSelector = new TwinColSelect<>("Select Your Problems");
        problemSelector.setLeftColumnCaption("Available Problems");
        problemSelector.setRightColumnCaption("Selected Problems");
        final List<String> availableProblems = this.service.getAvailableProblems();
        problemSelector.setDataProvider(DataProvider.ofCollection(availableProblems));


        problemSelector.addSelectionListener(new MultiSelectionListener<String>() {
            @Override
            public void selectionChange(MultiSelectionEvent<String> event) {
                selectedProblems.clear();
                selectedProblems.addAll(event.getAllSelectedItems());
            }
        });


        Button addAllProblemsBtn = new Button("Add All");
        addAllProblemsBtn.addClickListener(generateAddAllClickListener(availableProblems, problemSelector));

        _problemsContainer.addComponent(problemSelector);
        _problemsContainer.setComponentAlignment(problemSelector, Alignment.TOP_CENTER);
        _problemsContainer.addComponent(addAllProblemsBtn);
        _problemsContainer.setComponentAlignment(addAllProblemsBtn, Alignment.MIDDLE_RIGHT);
    }

    public static void setAlignemntToAllComponents(AbstractOrderedLayout layout, Alignment alignment) {
        Iterator<Component> componentIterator = layout.iterator();
        while (componentIterator.hasNext())
        {
            Component currentComponent = componentIterator.next();
            layout.setComponentAlignment(currentComponent, alignment);
        }
    }



    private Button.ClickListener generateAddAllClickListener(List<String> items, AbstractMultiSelect component)
    {
        return new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                for (String item : items)
                {
                    component.select(item);
                }
            }
        };
    }


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

        final String COMPILED_ALGO_DIR = "target/classes/FinalProject/BL/Agents";
        VerticalLayout layout = new VerticalLayout(dropArea);
        layout.setStyleName("drop-area");
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
                    Notification.show("Stream failed, fileName="
                            + event.getFileName());
                }
                @Override
                public boolean isInterrupted() {
                    return false;
                }

                private void algoUploadFinished(String fileName) {
                    try {
                        service.addNewAlgo(COMPILED_ALGO_DIR, fileName);
                        refreshAlgorithms();
                        Notification.show(fileName + " was added successfully!");
                        //TODO clean catches
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        System.err.println(e.getMessage());
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
        service.setProblemsToExperiment(selectedProblems);
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
            selectedProblems.forEach(problem -> problemAlgoPairs.add(new ProblemAlgoPair(algo, problem)));
        });

        experimentRunningPresenter.setAlgorithmProblemPairs(problemAlgoPairs);
    }
}
