package FinalProject.PL;

import FinalProject.BL.DataCollection.AlgorithmProblemResult;
import com.jarektoro.responsivelayout.ResponsiveLayout;
import com.jarektoro.responsivelayout.ResponsiveRow;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Responsive;
import com.vaadin.ui.*;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.StatisticalLineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.statistics.DefaultStatisticalCategoryDataset;
import org.vaadin.addon.JFreeChartWrapper;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;


public class ExperimentResultsPresenter extends Panel implements View{

    private final Navigator navigator;

    private Map<String, DefaultStatisticalCategoryDataset> powerConsumptionGraph = new HashMap<>();
    private Map<String, DefaultStatisticalCategoryDataset> powerConsumptionAnyTimeGraph = new HashMap<>();
    private Map<String, DefaultStatisticalCategoryDataset> highestAgentGraph = new HashMap<>();
    private Map<String, DefaultStatisticalCategoryDataset> lowestAgentGraph = new HashMap<>();
    private Map<String, DefaultStatisticalCategoryDataset> averageExperimentTime = new HashMap<>();
    private static DefaultCategoryDataset messagesNumPerAlgo;
    private static DefaultCategoryDataset messagesSizeAvePerAlgo;
    private static List<AlgorithmProblemResult> numOfAlgos;
    private final String noErrorBars= "No Error Bars";
    private static Set<String> comboNames;
    private static final Logger logger = Logger.getLogger(ExperimentResultsPresenter.class);

    public ExperimentResultsPresenter(Navigator navigator)
    {
        this.navigator = navigator;
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {

        logger.debug("enter");
        getUI().access(() -> {
            try {

                Map<String,ResponsiveLayout> algoToLayout = new ConcurrentHashMap<>();
                this.comboNames = filterAlgoNames();

                //create graphs in parallel
                comboNames.parallelStream().forEach(algoName ->
                        algoToLayout.put(algoName, createResultsLayoutWithErrorsBars(algoName)));

                algoToLayout.put(noErrorBars, createResultsLayoutWithoutErrorsBars());
                comboNames.add(noErrorBars);

                Button endExperimentBtn = new Button("End Experiment");
                endExperimentBtn.addClickListener((Button.ClickListener) event1 -> getUI().access(() ->{
                   getUI().getNavigator().navigateTo(UiHandler.EXPERIMENT_CONFIGURATION);
                   reset();
                }));

                ComboBox<String> select =
                        new ComboBox<>("Select error bars show");
                select.setItems(comboNames);

                AtomicReference<ResponsiveLayout> runningLayout = new AtomicReference<>(algoToLayout.get(noErrorBars));
                runningLayout.get().setVisible(true);
                Label topLabel = new Label("Results:");
                topLabel.addStyleName("v-label-h1");
                topLabel.addStyleName("conf-title");
                VerticalLayout mainLayout = new VerticalLayout();

                mainLayout.addComponents(topLabel, select);
                for(Map.Entry<String,ResponsiveLayout> entry: algoToLayout.entrySet()){
                    mainLayout.addComponents(entry.getValue());
                    if (!entry.getKey().equals(noErrorBars))
                    {
                        entry.getValue().setVisible(false);
                    }
                }
                mainLayout.addComponents(endExperimentBtn);
                mainLayout.setSizeUndefined();
                mainLayout.setComponentAlignment(select, Alignment.TOP_CENTER);
                mainLayout.setComponentAlignment(topLabel, Alignment.TOP_CENTER);
                mainLayout.setComponentAlignment(endExperimentBtn, Alignment.BOTTOM_CENTER);

                Responsive.makeResponsive(mainLayout);


                select.addValueChangeListener(event2 -> {
                    if (event2.getSource().isEmpty()) {
                        new Notification("You didn't choose nothing");
                    } else {
                        runningLayout.get().setVisible(false);
                        runningLayout.set(algoToLayout.get(event2.getValue()));
                        runningLayout.get().setVisible(true);

                    }
                });

                setContent(mainLayout);
            }
            catch (Exception e) {
                logger.error("failed setting the contents of screen with exception ", e);
                navigator.navigateTo(UiHandler.EXPERIMENT_CONFIGURATION);
            }

        });
    }

    private void reset() {
        powerConsumptionGraph = new HashMap<>();
        powerConsumptionAnyTimeGraph = new HashMap<>();
        highestAgentGraph = new HashMap<>();
        lowestAgentGraph = new HashMap<>();
        averageExperimentTime = new HashMap<>();
        messagesNumPerAlgo = null;
        messagesSizeAvePerAlgo = null;
        numOfAlgos = null;
        comboNames = null;
    }
    
    private Set<String> filterAlgoNames() {
        Set<String> results = new HashSet<>();
        this.numOfAlgos.forEach(algo -> {
            String algoName = algo.getAlgorithm();
            if (!results.contains(algo)){
                results.add(algoName);
            }
        });

        return results;
    }

    private void addStyleToChartAndAddToMainRow(Component component, ResponsiveRow mainRow) {
        component.addStyleName("result-chart");
        mainRow.addComponent(component);
    }
    
    private ResponsiveLayout createResultsLayoutWithErrorsBars(String algoName) {
        ResponsiveLayout resultsLayout = new ResponsiveLayout()
                .withSpacing()
                .withFullSize();
        ResponsiveRow mainRow = resultsLayout.addRow()
                .withVerticalSpacing(true)
                .withAlignment(Alignment.MIDDLE_CENTER);
        
        CompletableFuture totalGradeWithErrorBar = CompletableFuture.supplyAsync(() ->
                generateLineGraphWithErrorBars("Total grade per iteration #", "Iteration #", "Average Cost", powerConsumptionGraph.get(algoName), false))
                .thenAccept(chart -> addStyleToChartAndAddToMainRow(chart, mainRow));
        CompletableFuture totalGradeWithErrorBarAnyTime = CompletableFuture.supplyAsync(() ->
                generateLineGraphWithErrorBars("Total BEST grade per iteration #", "Iteration #", "Average Cost", powerConsumptionAnyTimeGraph.get(algoName), false))
                .thenAccept(chart -> addStyleToChartAndAddToMainRow(chart, mainRow));
        CompletableFuture cheapestAgentWithErrorBar = CompletableFuture.supplyAsync(() ->
                generateLineGraphWithErrorBars("Cheapest Agent By Iteration #", "Iteration #", "Cheapest Agent", lowestAgentGraph.get(algoName), false))
                .thenAccept(chart -> addStyleToChartAndAddToMainRow(chart, mainRow));
        CompletableFuture avgMsgSize = CompletableFuture.supplyAsync(() ->
                generateBarChart("Average messages size (Byte) per Algorithm #", null, null, messagesSizeAvePerAlgo))
                .thenAccept(chart -> addStyleToChartAndAddToMainRow(chart, mainRow));
        CompletableFuture mostExpensiveAgentWithErrorBar = CompletableFuture.supplyAsync(() ->
                generateLineGraphWithErrorBars("Most Expensive Agent By Iteration #", "Iteration #", "Most Expensive Agent", highestAgentGraph.get(algoName), false))
                .thenAccept(chart -> addStyleToChartAndAddToMainRow(chart, mainRow));
        CompletableFuture avgRunTimeWithErrorBar = CompletableFuture.supplyAsync(() ->
                generateLineGraphWithErrorBars("Average run time per iteration #", "Iteration #", "ms", averageExperimentTime.get(algoName), false))
                .thenAccept(chart -> addStyleToChartAndAddToMainRow(chart, mainRow));
        CompletableFuture avgNumMsgs = CompletableFuture.supplyAsync(() ->
                generateBarChart("Average number of messages per Algorithm #", null, null, messagesNumPerAlgo))
                .thenAccept(chart -> addStyleToChartAndAddToMainRow(chart, mainRow));

        CompletableFuture allDone = CompletableFuture.allOf(totalGradeWithErrorBar, totalGradeWithErrorBarAnyTime, cheapestAgentWithErrorBar,
                avgMsgSize, mostExpensiveAgentWithErrorBar, avgRunTimeWithErrorBar, avgNumMsgs);
        try {
            allDone.get();
        } catch (InterruptedException | ExecutionException e) {
            logger.error("error creating charts!");
        }

        return resultsLayout;
    }

    private ResponsiveLayout createResultsLayoutWithoutErrorsBars() {
        Iterator iter = this.comboNames.iterator();
        Object first = iter.next();
        Component totalGradeWithoutErrorBar = generateLineGraphWithoutErrorBars("Total grade per iteration #", "Iteration #", "Average Cost", powerConsumptionGraph.get(first), false);
        Component totalGradeWithErrorBarAnyTime = generateLineGraphWithoutErrorBars("Total BEST grade per iteration #", "Iteration #", "Average Cost", powerConsumptionAnyTimeGraph.get(first), false);
        Component cheapestAgentWithoutErrorBar = generateLineGraphWithoutErrorBars("Cheapest Agent By Iteration #", "Iteration #", "Cheapest Agent", lowestAgentGraph.get(first), false);
        Component avgMsgSize = generateBarChart("Average messages size (Byte) per Algorithm #", null, null, messagesSizeAvePerAlgo);
        Component mostExpensiveAgentWithoutErrorBar = generateLineGraphWithoutErrorBars("Most Expensive Agent By Iteration #", "Iteration #", "Most Expensive Agent", highestAgentGraph.get(first), false);
        Component avgRunTimeWithoutErrorBar = generateLineGraphWithoutErrorBars("Average run time per iteration #", "Iteration #", "ms", averageExperimentTime.get(first), false);
        Component avgNumMsgs = generateBarChart("Average number of messages per Algorithm #", null, null, messagesNumPerAlgo);


        ResponsiveLayout resultsLayout = new ResponsiveLayout()
                .withSpacing()
                .withFullSize();
        ResponsiveRow mainRow = resultsLayout.addRow()
                .withVerticalSpacing(true)
                .withAlignment(Alignment.MIDDLE_CENTER);
        List<Component> graphsLst = Arrays.asList(totalGradeWithoutErrorBar, totalGradeWithErrorBarAnyTime, cheapestAgentWithoutErrorBar, mostExpensiveAgentWithoutErrorBar,
                avgMsgSize, avgNumMsgs, avgRunTimeWithoutErrorBar);
        graphsLst.forEach(component -> {
            component.addStyleName("result-chart");
            mainRow.addComponent(component);
        });
        return resultsLayout;
    }

    public void addPowerConsumptionGraph(String algoName, DefaultStatisticalCategoryDataset powerCons)
    {
        this.powerConsumptionGraph.put(algoName,powerCons);
    }

    public void addPowerConsumptionAnyTimeGraph(String algoName, DefaultStatisticalCategoryDataset powerConsAnyTime)
    {
        this.powerConsumptionAnyTimeGraph.put(algoName,powerConsAnyTime);
    }

    public void addHighestAgentGrapthGrapth(String algoName, DefaultStatisticalCategoryDataset highestAgent)
    {
        this.highestAgentGraph.put(algoName,highestAgent);
    }

    public void addLowestAgentGrapthGrapth(String algoName, DefaultStatisticalCategoryDataset lowestAgent)
    {
        this.lowestAgentGraph.put(algoName,lowestAgent);
    }

    public void addAverageExperimentTime(String algoName, DefaultStatisticalCategoryDataset aveTime)
    {
        this.averageExperimentTime.put(algoName,aveTime);
    }

    public void setMessagesSentPerIteration(DefaultCategoryDataset messagesSentPerIteration)
    {
        this.messagesNumPerAlgo = messagesSentPerIteration;
    }

    public void setMessagesSizePerAlgo(DefaultCategoryDataset messagesSizeAvePerAlgo)
    {
        this.messagesSizeAvePerAlgo = messagesSizeAvePerAlgo;
    }

    public void setNumOfAlgos(List<AlgorithmProblemResult> algoNum)
    {
        this.numOfAlgos = algoNum;
    }



    private Component generateLineGraphWithErrorBars(String title, String xAxisLabel, String yAxisLabel, DefaultStatisticalCategoryDataset dataset, boolean shapesIsVisible) {
        JFreeChart plot = ChartFactory.createLineChart(title, xAxisLabel, yAxisLabel, dataset, PlotOrientation.VERTICAL, true, true, true);
        StatisticalLineAndShapeRenderer statisticalRenderer = new StatisticalLineAndShapeRenderer(true, false);
        statisticalRenderer.setErrorIndicatorPaint(Color.BLACK);

        plot.getCategoryPlot().setRenderer(statisticalRenderer);
        plot.setBackgroundPaint(Color.white);

        double lowerBound = getLowerBound(dataset, true);
        plot.getCategoryPlot().getRangeAxis().setLowerBound(lowerBound);

        // get a reference to the plot for further customisation...
        CategoryPlot plot1 = (CategoryPlot) plot.getPlot();
        plot1.setBackgroundPaint(Color.white);
        plot1.setDomainGridlinePaint(Color.white);
        plot1.setDomainGridlinesVisible(true);
        plot1.setRangeGridlinePaint(Color.white);
        plot1.setDomainGridlinePaint(Color.white);

        CategoryItemRenderer renderer = plot1.getRenderer();
        GradientPaint gp0 = new GradientPaint(50f, 50f, Color.CYAN, 50f, 50f, Color.black);
        GradientPaint gp1 = new GradientPaint(50f, 50f, Color.red, 50f, 50f, Color.BLUE);

        renderer.setSeriesPaint(0, gp0);
        renderer.setSeriesPaint(1, gp1);
        return new JFreeChartWrapper(plot);
    }

    private double getLowerBound(DefaultStatisticalCategoryDataset dataset, boolean withErrBars) {
        double lowerBound = dataset.getRangeBounds(false).getLowerBound();
        //if there are error bars, we need to adjust the low bound to contain them
        if (withErrBars) {
            double stdDev = 0;

            for (int row = 0; row < dataset.getRowCount(); row++) {
                for (int col = 0; col < dataset.getColumnCount(); col++) {
                    Number currStdDev = dataset.getStdDevValue(row, col);
                    if (currStdDev != null && currStdDev.doubleValue() > stdDev) {
                        stdDev = currStdDev.doubleValue();
                    }
                }
            }
            lowerBound -= stdDev;
        }
        //add 5% padding below
        lowerBound -= lowerBound / 20;

        return lowerBound;
    }

    private Component generateLineGraphWithoutErrorBars(String title, String xAxisLabel, String yAxisLabel, DefaultStatisticalCategoryDataset dataset, boolean shapesIsVisible) {
        JFreeChart plot = ChartFactory.createLineChart(title, xAxisLabel, yAxisLabel, dataset, PlotOrientation.VERTICAL, true, true, true);
        plot.setBackgroundPaint(Color.white);

        double lowerBound = getLowerBound(dataset, false);
        plot.getCategoryPlot().getRangeAxis().setLowerBound(lowerBound);

        // get a reference to the plot for further customisation...
        CategoryPlot plot1 = (CategoryPlot) plot.getPlot();
        plot1.setBackgroundPaint(Color.white);
        plot1.setDomainGridlinePaint(Color.white);
        plot1.setDomainGridlinesVisible(true);
        plot1.setRangeGridlinePaint(Color.white);
        plot1.setDomainGridlinePaint(Color.white);

        CategoryItemRenderer renderer = plot1.getRenderer();
        GradientPaint gp0 = new GradientPaint(50f, 50f, Color.CYAN, 50f, 50f, Color.green);
        GradientPaint gp1 = new GradientPaint(50f, 50f, Color.red, 50f, 50f, Color.BLUE);

        renderer.setSeriesPaint(0, gp0);
        renderer.setSeriesPaint(1, gp1);
        return new JFreeChartWrapper(plot);
    }

    private Component generateBarChart(String title, String xAxisLabel, String yAxisLabel, DefaultCategoryDataset dataset)
    {
        JFreeChart barChart = ChartFactory.createBarChart(title, xAxisLabel, yAxisLabel, dataset, PlotOrientation.VERTICAL, true, false, false);
        barChart.setBackgroundPaint(Color.white);
        barChart.setBorderPaint(Color.white);
        CategoryPlot plot1 = (CategoryPlot) barChart.getPlot();
        plot1.setBackgroundPaint(Color.white);
        plot1.setDomainGridlinePaint(Color.white);
        plot1.setDomainGridlinesVisible(true);
        plot1.setRangeGridlinePaint(Color.white);
        plot1.setDomainGridlinePaint(Color.white);
        return new JFreeChartWrapper(barChart);
    }
}
