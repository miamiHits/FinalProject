package FinalProject.PL;

import FinalProject.PL.UIEntities.ProblemAlgoPair;
import FinalProject.Service;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.event.selection.MultiSelectionEvent;
import com.vaadin.event.selection.MultiSelectionListener;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

        Label subtitleLbl = new Label("Smart Home Agent Simulator");

        mainLayout.addComponent(mainTitleLbl);
        mainLayout.addComponent(subtitleLbl);

        mainLayout.addComponent(configurationLayout);

        startExperimentBtn.addClickListener(this);

        mainLayout.addComponent(startExperimentBtn);
        setAlignemntToAllComponents(mainLayout, Alignment.MIDDLE_CENTER);

        setContent(mainLayout);

        setSizeFull();
    }


    private void generateAlgorithmsSection()
    {

        final TwinColSelect<String> algorithmSelector = new TwinColSelect<>("Select Your Algorithms");
        algorithmSelector.setLeftColumnCaption("Available Algorithms");
        algorithmSelector.setRightColumnCaption("Selected Algorithms");
        final List<String> availableAlgorithms = this.service.getAvailableAlgorithms();
        algorithmSelector.setDataProvider(DataProvider.ofCollection(availableAlgorithms));

        algorithmSelector.addSelectionListener(new MultiSelectionListener<String>() {
            @Override
            public void selectionChange(MultiSelectionEvent<String> event) {
                selectedAlgorithms.clear();
                selectedAlgorithms.addAll(event.getAllSelectedItems());
            }
        });

        Button addAllAlgorithmsBtn = new Button("Add All");
        addAllAlgorithmsBtn.addClickListener(generateAddAllClickListener(availableAlgorithms, algorithmSelector));

        Button loadSelectedAlgorithmsBtn = new Button("Load Algorithms");

        _algorithmsContainer.addComponent(algorithmSelector);
        _algorithmsContainer.addComponent(addAllAlgorithmsBtn);
        _algorithmsContainer.addComponent(numberOfIterationsTxt);
        _algorithmsContainer.addComponent(loadSelectedAlgorithmsBtn);
        _algorithmsContainer.addComponent(addNewAlgorithmBtn);

        setAlignemntToAllComponents(_algorithmsContainer, Alignment.MIDDLE_CENTER);

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

        Button loadSelectedProblemsBtn = new Button("Load Problems");

        _problemsContainer.addComponent(problemSelector);
        _problemsContainer.addComponent(addAllProblemsBtn);
        _problemsContainer.addComponent(loadSelectedProblemsBtn);
        _problemsContainer.addComponent(addAllProblemsBtn);

        setAlignemntToAllComponents(_problemsContainer, Alignment.MIDDLE_CENTER);
    }

    private static void setAlignemntToAllComponents(AbstractOrderedLayout layout, Alignment alignment) {
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
        else if (clickedButton.equals(addNewAlgorithmBtn))
        {

        }
    }

    private void startExperimentClicked()
    {

        int numberOfIterations = parseIterationNumber();

        setExperimentRunningPairs();

        service.setAlgorithmsToExperiment(selectedAlgorithms, numberOfIterations);
        service.setProblemsToExperiment(selectedProblems);
        service.runExperiment();

        getUI().getNavigator().navigateTo(UiHandler.EXPERIMENT_RUNNING);

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
