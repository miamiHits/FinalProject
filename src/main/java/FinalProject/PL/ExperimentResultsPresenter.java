package FinalProject.PL;

import com.jarektoro.responsivelayout.ResponsiveLayout;
import com.jarektoro.responsivelayout.ResponsiveRow;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Responsive;
import com.vaadin.ui.*;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.StatisticalLineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.statistics.DefaultStatisticalCategoryDataset;
import org.vaadin.addon.JFreeChartWrapper;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;


public class ExperimentResultsPresenter extends Panel implements View{

    private DefaultStatisticalCategoryDataset powerConsumptionGraph;
    private DefaultStatisticalCategoryDataset highestAgentGraph;
    private DefaultStatisticalCategoryDataset lowestAgentGraph;
    private DefaultStatisticalCategoryDataset averageExperimentTime;
    private DefaultCategoryDataset messagesNumPerAlgo;
    private DefaultCategoryDataset messagesSizeAvePerAlgo;

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {

        getUI().access(() -> {
            try {
                ResponsiveLayout resultsLayout = createResultsLayout();

                Button endExperimentBtn = new Button("End Experiment");
                endExperimentBtn.addClickListener((Button.ClickListener) event1 -> getUI().access(() ->{
                   getUI().getNavigator().navigateTo(UiHandler.EXPERIMENT_CONFIGURATION);
                   //TODO gal any export action required here?
                    }));

                Label topLabel = new Label("Results:");
                topLabel.addStyleName("v-label-h1");
                topLabel.addStyleName("conf-title");

                VerticalLayout mainLayout = new VerticalLayout();

                mainLayout.addComponents(topLabel, resultsLayout, endExperimentBtn);
                mainLayout.setSizeUndefined();
                mainLayout.setComponentAlignment(topLabel, Alignment.TOP_CENTER);
                mainLayout.setComponentAlignment(endExperimentBtn, Alignment.TOP_CENTER);
                Responsive.makeResponsive(mainLayout);

                setContent(mainLayout);
            }
            catch (Exception e) {
                e.printStackTrace();
                UiHandler.navigator.navigateTo(UiHandler.EXPERIMENT_CONFIGURATION);
            }

        });
    }

    private ResponsiveLayout createResultsLayout() {
        Component totalGrade = generateLineGraphWithErrorBars("Total grade per iteration #", "Iteration #", "Average Cost", powerConsumptionGraph, false);
        Component cheapestAgent = generateLineGraphWithErrorBars("Cheapest Agent By Iteration #", "Iteration #", "Cheapest Agent", lowestAgentGraph, false);
        Component avgMsgSize = generateBarChart("Average messages size (Byte) per Algorithm #", null, null, messagesSizeAvePerAlgo);
        Component mostExpensiveAgent = generateLineGraphWithErrorBars("Most Expensive Agent By Iteration #", "Iteration #", "Most Expensive Agent", highestAgentGraph, false);
        Component avgRunTime = generateLineGraphWithErrorBars("Average run time per iteration #", "Iteration #", "ms", averageExperimentTime, false);
        Component avgNumMsgs = generateBarChart("Average number of messages per Algorithm #", null, null, messagesNumPerAlgo);

        ResponsiveLayout resultsLayout = new ResponsiveLayout()
                .withSpacing()
                .withFullSize();
        ResponsiveRow mainRow = resultsLayout.addRow()
                .withVerticalSpacing(true)
                .withAlignment(Alignment.MIDDLE_CENTER);
        List<Component> graphsLst = Arrays.asList(totalGrade, cheapestAgent, mostExpensiveAgent,
                avgMsgSize, avgNumMsgs, avgRunTime);
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
       /* statisticalRenderer.setBaseItemLabelGenerator(
                new StandardCategoryItemLabelGenerator("{1}",
                        NumberFormat.getNumberInstance()));*/
        statisticalRenderer.setErrorIndicatorPaint(Color.white);
        //statisticalRenderer.setBaseItemLabelsVisible(true);
        //statisticalRenderer.setSeriesShape(0, new Rectangle2D.Double(0, 0, 0, 0));
        //statisticalRenderer.setBasePaint(Color.white);
       // statisticalRenderer.setBaseOutlinePaint(Color.white);
       // statisticalRenderer.setBaseLegendTextPaint(Color.white);

       plot.getCategoryPlot().setRenderer(statisticalRenderer);
        /* CategoryPlot chart = (CategoryPlot) plot.getPlot();
        CategoryAxis cx = new CategoryAxis();
        cx.setTickLabelsVisible(true);
        cx.setTickMarksVisible(true);

        chart.setDomainAxis(cx);*/

        //Design
        // set the background color for the chart...
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
        //GradientPaint gp1 = new GradientPaint(0.0f, 0.0f, Color.green, 0.0f,
               // 0.0f, new Color(0, 64, 0));
        //GradientPaint gp2 = new GradientPaint(0.0f, 0.0f, Color.red, 0.0f,
              //  0.0f, new Color(64, 0, 0));



        renderer.setSeriesPaint(0, gp0);
        //renderer.setSeriesPaint(1, gp1);
        //renderer.setSeriesPaint(2, gp2);

        //CategoryAxis domainAxis = plot1.getDomainAxis();
        //domainAxis.setCategoryLabelPositions(CategoryLabelPositions
                //.createUpRotationLabelPositions(Math.PI / 6.0));



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
