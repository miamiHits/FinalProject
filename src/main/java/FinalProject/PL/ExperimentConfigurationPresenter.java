package FinalProject.PL;

import FinalProject.Service;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.*;

import java.util.Iterator;
import java.util.List;

public class ExperimentConfigurationPresenter extends Panel implements View {

    private VerticalLayout _algorithmsContainer;
    private VerticalLayout _problemsContainer;

    private Service service;

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {


    //TODO gal for final iteration prevent use of more than one browser tab
        _algorithmsContainer = new VerticalLayout();
        _problemsContainer = new VerticalLayout();

        this.service = UiHandler.service;

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

        Button startExperimentBtn = new Button("Start Experiment");

        startExperimentBtn.addClickListener(clickEvent -> UiHandler.navigator.navigateTo(UiHandler.EXPERIMENT_RESULTS));

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

        Button addAllAlgorithmsBtn = new Button("Add All");
        addAllAlgorithmsBtn.addClickListener(generateAddAllClickListener(availableAlgorithms, algorithmSelector));

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

}
