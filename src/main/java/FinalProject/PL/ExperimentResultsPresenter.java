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

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {


        JFreeChartUtils.ALGORITHM_COUNT = 3;

        final VerticalLayout layout = new VerticalLayout();

        layout.addComponent(generateLineGraphWithErrorBars("Average Cost By Iteration #", "Iteration #", "Average Cost", JFreeChartUtils.createLineChartDataset(), false));
        layout.addComponent(generateLineGraphWithErrorBars("Cheapest Agent By Iteration #", "Iteration #", "Cheapest Agent", JFreeChartUtils.createLineChartDataset(), false));
        layout.addComponent(generateLineGraphWithErrorBars("Most Expensive Agent By Iteration #", "Iteration #", "Most Expensive Agent", JFreeChartUtils.createLineChartDataset(), false));

        layout.addComponent(generateBarChart("Runtime Statistics", null, null, JFreeChartUtils.createBarChartDataset()));

        setContent(layout);
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
