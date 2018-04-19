package FinalProject.PL;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Responsive;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.*;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
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
import java.awt.GridLayout;


public class ExperimentResultsPresenter extends Panel implements View{

    private DefaultStatisticalCategoryDataset powerConsumptionGraph;
    private DefaultStatisticalCategoryDataset highestAgentGraph;
    private DefaultStatisticalCategoryDataset lowestAgentGraph;
    private DefaultCategoryDataset averageExperimentTime;
    private DefaultCategoryDataset messagesSentPerIteration;

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {

        getUI().access(() -> {

            try {

                final VerticalLayout leftGraphsLayout = new VerticalLayout();
                final VerticalLayout rightGraphsLayout = new VerticalLayout();
                VerticalLayout veryLeftGraphsLayout = new VerticalLayout();

                final HorizontalSplitPanel allGraphsLayout = new HorizontalSplitPanel();
                final VerticalLayout mainLayout = new VerticalLayout();
                Component c1 = generateLineGraphWithErrorBars("Total grade per iteration #", "Iteration #", "Average Cost", powerConsumptionGraph, false);
                Component c2 = generateLineGraphWithErrorBars("Cheapest Agent By Iteration #", "Iteration #", "Cheapest Agent", lowestAgentGraph, false);
                leftGraphsLayout.addComponent(generateLineGraphWithErrorBars("Total grade per iteration #", "Iteration #", "Average Cost", powerConsumptionGraph, false));
                leftGraphsLayout.addComponent(generateLineGraphWithErrorBars("Cheapest Agent By Iteration #", "Iteration #", "Cheapest Agent", lowestAgentGraph, false));
                rightGraphsLayout.addComponent(generateLineGraphWithErrorBars("Most Expensive Agent By Iteration #", "Iteration #", "Most Expensive Agent", highestAgentGraph, false));
                rightGraphsLayout.addComponent(generateBarChart("Average run time per Algorithm #", null, null, averageExperimentTime));
                rightGraphsLayout.addComponent(generateBarChart("Average sending messages per Algorithm #", null, null, messagesSentPerIteration));
                //allGraphsLayout.addComponents(c1);
                //allGraphsLayout.setExpandRatio(c1, 0.0f);
                //allGraphsLayout.addComponents(c2);
                //allGraphsLayout.setExpandRatio(c2, 3.0f);
                leftGraphsLayout.setWidth("100%");
                rightGraphsLayout.setWidth("100%");
                //veryLeftGraphsLayout.setWidth("100%");
                //allGraphsLayout.addComponent(leftGraphsLayout);

                //allGraphsLayout.addComponent(rightGraphsLayout);
                allGraphsLayout.setFirstComponent(leftGraphsLayout);
                allGraphsLayout.setSecondComponent(rightGraphsLayout);
                allGraphsLayout.setSplitPosition(50, Sizeable.UNITS_PERCENTAGE);

                //allGraphsLayout.setWidth("100%");
                //allGraphsLayout.setSizeFull();


                Button endExperimentBtn = new Button("End Experiment");
                endExperimentBtn.addClickListener(new Button.ClickListener() {
                    @Override
                    public void buttonClick(Button.ClickEvent event) {
                        getUI().access(() ->{
                           getUI().getNavigator().navigateTo(UiHandler.EXPERIMENT_CONFIGURATION);
                           //TODO gal any export action reauired here?
                        });
                    }
                });


                mainLayout.addComponents(allGraphsLayout);
                mainLayout.addComponent(endExperimentBtn);
                mainLayout.setSpacing(false);
                ExperimentConfigurationPresenter.setAlignemntToAllComponents(mainLayout, Alignment.TOP_CENTER);
                mainLayout.setWidth("100%");

                VerticalLayout layout = new VerticalLayout();
                Panel panelGraphs = new Panel("Graphs page", endExperimentBtn);

// Have a horizontal split panel as its content
                HorizontalSplitPanel hsplit = new HorizontalSplitPanel();
                panelGraphs.setContent(hsplit);

// Put a component in the left panel
                hsplit.setFirstComponent(leftGraphsLayout);

                hsplit.setSecondComponent(rightGraphsLayout);
                //Responsive.makeResponsive(panelGraphs);
                panelGraphs.setSizeUndefined(); // Shrink to fit content
                layout.addComponent(panelGraphs);
                layout.addComponent(endExperimentBtn);
                layout.setSizeUndefined();
                layout.setComponentAlignment(endExperimentBtn, Alignment.TOP_CENTER);
                Responsive.makeResponsive(layout);

                setContent(layout);


            }
            catch (Exception e)
            {
                e.printStackTrace();
                UiHandler.navigator.navigateTo(UiHandler.EXPERIMENT_CONFIGURATION);
            }

        });


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

    public void setAverageExperimentTime(DefaultCategoryDataset aveTime)
    {
        this.averageExperimentTime = aveTime;
    }

    public void setMessagesSentPerIteration(DefaultCategoryDataset messagesSentPerIteration)
    {
        this.messagesSentPerIteration = messagesSentPerIteration;
    }



    private Component generateLineGraphWithErrorBars(String title, String xAxisLabel, String yAxisLabel, DefaultStatisticalCategoryDataset dataset, boolean shapesIsVisible)
    {
        JFreeChart plot = ChartFactory.createLineChart(title, xAxisLabel, yAxisLabel, dataset, PlotOrientation.VERTICAL, true, true, true);
        StatisticalLineAndShapeRenderer statisticalRenderer = new StatisticalLineAndShapeRenderer(true, shapesIsVisible);
        statisticalRenderer.setErrorIndicatorPaint(Color.white);
        plot.getCategoryPlot().setRenderer(statisticalRenderer);

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
