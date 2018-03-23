package FinalProject.PL;

import FinalProject.PL.JFreeChart.JFreeChartUtils;
import FinalProject.PL.UIEntities.ProblemAlgoPair;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.*;
import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;

import com.vaadin.server.VaadinRequest;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.StatisticalLineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.statistics.DefaultStatisticalCategoryDataset;
import org.vaadin.addon.JFreeChartWrapper;

import java.awt.*;


public class ExperimentResultsPresenter extends Panel implements View{

    private DefaultStatisticalCategoryDataset powerConsumptionGrapth;
    private DefaultStatisticalCategoryDataset highestAgentGrapth;
    private DefaultStatisticalCategoryDataset lowestAgentGrapth;
    private DefaultCategoryDataset averageExperimentTime;

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {

        getUI().access(new Runnable() {
            @Override
            public void run() {

                try {

                    final VerticalLayout layout = new VerticalLayout();

                    layout.addComponent(generateLineGraphWithErrorBars("Average Cost By Iteration #", "Iteration #", "Average Cost", powerConsumptionGrapth, false));
                    layout.addComponent(generateLineGraphWithErrorBars("Cheapest Agent By Iteration #", "Iteration #", "Cheapest Agent", lowestAgentGrapth, false));
                    layout.addComponent(generateLineGraphWithErrorBars("Most Expensive Agent By Iteration #", "Iteration #", "Most Expensive Agent", highestAgentGrapth, false));

                    layout.addComponent(generateBarChart("Runtime Average time Statistics", null, null, averageExperimentTime));

                    setContent(layout);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    UiHandler.navigator.navigateTo(UiHandler.EXPERIMENT_CONFIGURATION);
                }

            }
        });

    }

    public void setPowerConsumptionGrapth(DefaultStatisticalCategoryDataset powerCons)
    {
        this.powerConsumptionGrapth = powerCons;
    }

    public void setHighestAgentGrapthGrapth(DefaultStatisticalCategoryDataset highestAgent)
    {
        this.highestAgentGrapth = highestAgent;
    }

    public void setLowestAgentGrapthGrapth(DefaultStatisticalCategoryDataset lowestAgent)
    {
        this.lowestAgentGrapth = lowestAgent;
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
