package FinalProject.PL;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.StatisticalLineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.statistics.DefaultStatisticalCategoryDataset;
import org.vaadin.addon.JFreeChartWrapper;

import java.awt.*;

//TODO gal add return to main screen button

public class ExperimentResultsPresenter extends Panel implements View{

    private DefaultStatisticalCategoryDataset powerConsumptionGraph;
    private DefaultStatisticalCategoryDataset highestAgentGraph;
    private DefaultStatisticalCategoryDataset lowestAgentGraph;
    private DefaultCategoryDataset averageExperimentTime;

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {

        getUI().access(() -> {

            try {

                final VerticalLayout layout = new VerticalLayout();

                layout.addComponent(generateLineGraphWithErrorBars("Average Cost By Iteration #", "Iteration #", "Average Cost", powerConsumptionGraph, false));
                layout.addComponent(generateLineGraphWithErrorBars("Cheapest Agent By Iteration #", "Iteration #", "Cheapest Agent", lowestAgentGraph, false));
                layout.addComponent(generateLineGraphWithErrorBars("Most Expensive Agent By Iteration #", "Iteration #", "Most Expensive Agent", highestAgentGraph, false));

                layout.addComponent(generateBarChart("Runtime Average time Statistics", null, null, averageExperimentTime));

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



    private Component generateLineGraphWithErrorBars(String title, String xAxisLabel, String yAxisLabel, DefaultStatisticalCategoryDataset dataset, boolean shapesIsVisible)
    {
        JFreeChart plot = ChartFactory.createLineChart(title, xAxisLabel, yAxisLabel, dataset, PlotOrientation.VERTICAL, true, false, false);
        StatisticalLineAndShapeRenderer statisticalRenderer = new StatisticalLineAndShapeRenderer(true, shapesIsVisible);
        statisticalRenderer.setErrorIndicatorPaint(Color.black);
        plot.getCategoryPlot().setRenderer(statisticalRenderer);
        return new JFreeChartWrapper(plot);
    }

    private Component generateBarChart(String title, String xAxisLabel, String yAxisLabel, DefaultCategoryDataset dataset)
    {
        JFreeChart barChart = ChartFactory.createBarChart(title, xAxisLabel, yAxisLabel, dataset, PlotOrientation.VERTICAL, true, false, false);
        return new JFreeChartWrapper(barChart);

    }




}
