package FinalProject.PL;

import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.*;

import java.util.Iterator;

public class ExperimentConfigurationPresenter extends UI {

    private VerticalLayout _algorithmsContainer;
    private VerticalLayout _problemsContainer;


    @Override
    protected void init(VaadinRequest vaadinRequest) {

        final VerticalLayout mainLayout = new VerticalLayout();
        _algorithmsContainer = new VerticalLayout();
        _problemsContainer = new VerticalLayout();

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

        Button startExperimentBtn = new Button("Start Experiment");

        mainLayout.addComponent(startExperimentBtn);
        setAlignemntToAllComponents(mainLayout, Alignment.MIDDLE_CENTER);

        setContent(mainLayout);
    }

    private void generateAlgorithmsSection()
    {

        TwinColSelect<String> algorithmSelector = new TwinColSelect<>("Select Your Algorithms");
        algorithmSelector.setLeftColumnCaption("Available Algorithms");
        algorithmSelector.setRightColumnCaption("Selected Algorithms");

        Button addAllAlgorithmsBtn = new Button("Add All");

        TextField numberOfIterationsTxt = new TextField();
        numberOfIterationsTxt.setCaption("Select Number of Iterations");

        Button loadSelectedAlgorithmsBtn = new Button("Load Algorithms");

        Button addNewAlgorithmBtn = new Button("Add New Algorithm");



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

        Button addAllProblemsBtn = new Button("Add All");
        //TODO implement click listener

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

}
