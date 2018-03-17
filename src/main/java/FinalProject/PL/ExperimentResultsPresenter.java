package FinalProject.PL;

import FinalProject.PL.JFreeChart.JFreeChartUtils;
import org.jfree.chart.JFreeChart;

import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Component;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import org.vaadin.addon.JFreeChartWrapper;


public class ExperimentResultsPresenter extends UI{

    @Override
    protected void init(VaadinRequest request) {
        final VerticalLayout layout = new VerticalLayout();
        layout.addComponent(getTestAndDemos());
        setContent(layout);
    }

    public static JFreeChartWrapper createBasicDemo() {
        JFreeChart createchart = JFreeChartUtils.createchart(JFreeChartUtils.createDataset());
        return new JFreeChartWrapper(createchart);
    }



    /**
     * Static
     *
     * @return
     */
    public static Component getTestAndDemos() {
        VerticalLayout vl = new VerticalLayout();
        vl.setSpacing(true);

        vl.addComponent(createBasicDemo());

        vl.addComponent(JFreeChartUtils.getLevelChart());

        vl.addComponent(JFreeChartUtils.regressionChart());

        return vl;
    }



}
