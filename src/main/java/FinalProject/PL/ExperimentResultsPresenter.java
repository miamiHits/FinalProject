package FinalProject.PL;

import com.jarektoro.responsivelayout.ResponsiveLayout;
import com.jarektoro.responsivelayout.ResponsiveRow;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Responsive;
import com.vaadin.shared.Registration;
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
import java.util.Arrays;
import java.util.List;


public class ExperimentResultsPresenter extends Panel implements View{

    private DefaultStatisticalCategoryDataset powerConsumptionGraph;
    private DefaultStatisticalCategoryDataset highestAgentGraph;
    private DefaultStatisticalCategoryDataset lowestAgentGraph;
    private DefaultStatisticalCategoryDataset averageExperimentTime;
    private DefaultCategoryDataset messagesNumPerAlgo;
    private DefaultCategoryDataset messagesSizeAvePerAlgo;

    private static final Logger logger = Logger.getLogger(ExperimentResultsPresenter.class);

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {

        logger.debug("enter");
        getUI().access(() -> {
            try {
                ResponsiveLayout resultsLayoutWithErrorsBars = createResultsLayoutWithErrorsBars();
                ResponsiveLayout resultsLayoutWithoutErrorsBars = createResultsLayoutWithoutErrorsBars();

                final boolean[] withErrorsBars = {true};
                Button endExperimentBtn = new Button("End Experiment");
                endExperimentBtn.addClickListener((Button.ClickListener) event1 -> getUI().access(() ->{
                   getUI().getNavigator().navigateTo(UiHandler.EXPERIMENT_CONFIGURATION);
                    }));

                Button withoutErrorsBars = new Button("With/Without Errors Bars");
                Registration registration = withoutErrorsBars.addClickListener((Button.ClickListener) event1 -> {
                    getUI().access(() -> {
                        if (withErrorsBars[0]) {
                            resultsLayoutWithErrorsBars.setVisible(false);
                            resultsLayoutWithoutErrorsBars.setVisible(true);
                            withErrorsBars[0] = false;
                        }
                        else{
                            resultsLayoutWithErrorsBars.setVisible(true);
                            resultsLayoutWithoutErrorsBars.setVisible(false);
                            withErrorsBars[0] = true;
                        }


                    });
                });

                resultsLayoutWithoutErrorsBars.setVisible(false);
                Label topLabel = new Label("Results:");
                topLabel.addStyleName("v-label-h1");
                topLabel.addStyleName("conf-title");

                VerticalLayout mainLayout = new VerticalLayout();

                mainLayout.addComponents(topLabel, resultsLayoutWithErrorsBars, resultsLayoutWithoutErrorsBars,
                        endExperimentBtn, withoutErrorsBars);
                mainLayout.setSizeUndefined();
                mainLayout.setComponentAlignment(topLabel, Alignment.TOP_CENTER);
                mainLayout.setComponentAlignment(endExperimentBtn, Alignment.TOP_CENTER);
                mainLayout.setComponentAlignment(withoutErrorsBars, Alignment.TOP_CENTER);

                Responsive.makeResponsive(mainLayout);

                setContent(mainLayout);
            }
            catch (Exception e) {
                logger.error("failed setting the contents of screen with exception ", e);
                UiHandler.navigator.navigateTo(UiHandler.EXPERIMENT_CONFIGURATION);
            }

        });
    }

    private ResponsiveLayout createResultsLayoutWithErrorsBars() {
        Component totalGradeWithErrorBar = generateLineGraphWithErrorBars("Total grade per iteration #", "Iteration #", "Average Cost", powerConsumptionGraph, false);
        Component cheapestAgentWithErrorBar = generateLineGraphWithErrorBars("Cheapest Agent By Iteration #", "Iteration #", "Cheapest Agent", lowestAgentGraph, false);
        Component avgMsgSize = generateBarChart("Average messages size (Byte) per Algorithm #", null, null, messagesSizeAvePerAlgo);
        Component mostExpensiveAgentWithErrorBar = generateLineGraphWithErrorBars("Most Expensive Agent By Iteration #", "Iteration #", "Most Expensive Agent", highestAgentGraph, false);
        Component avgRunTimeWithErrorBar = generateLineGraphWithErrorBars("Average run time per iteration #", "Iteration #", "ms", averageExperimentTime, false);
        Component avgNumMsgs = generateBarChart("Average number of messages per Algorithm #", null, null, messagesNumPerAlgo);

        ResponsiveLayout resultsLayout = new ResponsiveLayout()
                .withSpacing()
                .withFullSize();
        ResponsiveRow mainRow = resultsLayout.addRow()
                .withVerticalSpacing(true)
                .withAlignment(Alignment.MIDDLE_CENTER);
        List<Component> graphsLst = Arrays.asList(totalGradeWithErrorBar, cheapestAgentWithErrorBar, mostExpensiveAgentWithErrorBar,
                avgMsgSize, avgNumMsgs, avgRunTimeWithErrorBar);
        graphsLst.forEach(component -> {
            component.addStyleName("result-chart");
            mainRow.addComponent(component);
        });
        return resultsLayout;
    }

    private ResponsiveLayout createResultsLayoutWithoutErrorsBars() {
        Component totalGradeWithoutErrorBar = generateLineGraphWithoutErrorBars("Total grade per iteration #", "Iteration #", "Average Cost", powerConsumptionGraph, false);
        Component cheapestAgentWithoutErrorBar = generateLineGraphWithoutErrorBars("Cheapest Agent By Iteration #", "Iteration #", "Cheapest Agent", lowestAgentGraph, false);
        Component avgMsgSize = generateBarChart("Average messages size (Byte) per Algorithm #", null, null, messagesSizeAvePerAlgo);
        Component mostExpensiveAgentWithoutErrorBar = generateLineGraphWithoutErrorBars("Most Expensive Agent By Iteration #", "Iteration #", "Most Expensive Agent", highestAgentGraph, false);
        Component avgRunTimeWithoutErrorBar = generateLineGraphWithoutErrorBars("Average run time per iteration #", "Iteration #", "ms", averageExperimentTime, false);
        Component avgNumMsgs = generateBarChart("Average number of messages per Algorithm #", null, null, messagesNumPerAlgo);


        ResponsiveLayout resultsLayout = new ResponsiveLayout()
                .withSpacing()
                .withFullSize();
        ResponsiveRow mainRow = resultsLayout.addRow()
                .withVerticalSpacing(true)
                .withAlignment(Alignment.MIDDLE_CENTER);
        List<Component> graphsLst = Arrays.asList(totalGradeWithoutErrorBar, cheapestAgentWithoutErrorBar, mostExpensiveAgentWithoutErrorBar,
                avgMsgSize, avgNumMsgs, avgRunTimeWithoutErrorBar);
        graphsLst.forEach(component -> {
            component.addStyleName("result-chart");
            mainRow.addComponent(component);
        });
        return resultsLayout;
    }

    public void setPowerConsumptionGraph(DefaultStatisticalCategoryDataset powerCons)
    {
        this.powerConsumptionGraph = powerCons;
    }

    public void setHighestAgentGrapthGrapth(DefaultStatisticalCategoryDataset highestAgent)
    {
        this.highestAgentGraph = highestAgent;
    }

    public void setLowestAgentGrapthGrapth(DefaultStatisticalCategoryDataset lowestAgent)
    {
        this.lowestAgentGraph = lowestAgent;
    }

    public void setAverageExperimentTime(DefaultStatisticalCategoryDataset aveTime)
    {
        this.averageExperimentTime = aveTime;
    }

    public void setMessagesSentPerIteration(DefaultCategoryDataset messagesSentPerIteration)
    {
        this.messagesNumPerAlgo = messagesSentPerIteration;
    }

    public void setMessagesSizePerAlgo(DefaultCategoryDataset messagesSizeAvePerAlgo)
    {
        this.messagesSizeAvePerAlgo = messagesSizeAvePerAlgo;
    }



    private Component generateLineGraphWithErrorBars(String title, String xAxisLabel, String yAxisLabel, DefaultStatisticalCategoryDataset dataset, boolean shapesIsVisible) {
        JFreeChart plot = ChartFactory.createLineChart(title, xAxisLabel, yAxisLabel, dataset, PlotOrientation.VERTICAL, true, true, true);
        StatisticalLineAndShapeRenderer statisticalRenderer = new StatisticalLineAndShapeRenderer(true, false);
        statisticalRenderer.setErrorIndicatorPaint(Color.white);

        plot.getCategoryPlot().setRenderer(statisticalRenderer);
        plot.setBackgroundPaint(Color.white);


        // get a reference to the plot for further customisation...
        CategoryPlot plot1 = (CategoryPlot) plot.getPlot();
        plot1.setBackgroundPaint(Color.black);
        plot1.setDomainGridlinePaint(Color.white);
        plot1.setDomainGridlinesVisible(true);
        plot1.setRangeGridlinePaint(Color.white);
        plot1.setDomainGridlinePaint(Color.black);

        CategoryItemRenderer renderer = plot1.getRenderer();
        GradientPaint gp0 = new GradientPaint(50f, 50f, Color.CYAN, 50f, 50f, Color.green);
        renderer.setSeriesPaint(0, gp0);
        return new JFreeChartWrapper(plot);
    }

    private Component generateLineGraphWithoutErrorBars(String title, String xAxisLabel, String yAxisLabel, DefaultStatisticalCategoryDataset dataset, boolean shapesIsVisible) {
        JFreeChart plot = ChartFactory.createLineChart(title, xAxisLabel, yAxisLabel, dataset, PlotOrientation.VERTICAL, true, true, true);
        plot.setBackgroundPaint(Color.white);
        // get a reference to the plot for further customisation...
        CategoryPlot plot1 = (CategoryPlot) plot.getPlot();
        plot1.setBackgroundPaint(Color.black);
        plot1.setDomainGridlinePaint(Color.white);
        plot1.setDomainGridlinesVisible(true);
        plot1.setRangeGridlinePaint(Color.white);
        plot1.setDomainGridlinePaint(Color.black);

        CategoryItemRenderer renderer = plot1.getRenderer();
        GradientPaint gp0 = new GradientPaint(50f, 50f, Color.CYAN, 50f, 50f, Color.green);
        renderer.setSeriesPaint(0, gp0);
        return new JFreeChartWrapper(plot);
    }

    private Component generateBarChart(String title, String xAxisLabel, String yAxisLabel, DefaultCategoryDataset dataset)
    {
        JFreeChart barChart = ChartFactory.createBarChart(title, xAxisLabel, yAxisLabel, dataset, PlotOrientation.VERTICAL, true, false, false);
        barChart.setBackgroundPaint(Color.white);
        barChart.setBorderPaint(Color.white);
        CategoryPlot plot1 = (CategoryPlot) barChart.getPlot();
        plot1.setBackgroundPaint(Color.black);
        plot1.setDomainGridlinePaint(Color.white);
        plot1.setDomainGridlinesVisible(true);
        plot1.setRangeGridlinePaint(Color.white);
        plot1.setDomainGridlinePaint(Color.black);
        return new JFreeChartWrapper(barChart);
    }
}
