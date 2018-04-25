package FinalProject.BL.DataCollection;

import FinalProject.BL.Agents.SHMGM;
import FinalProjectTests.BL.Agents.ReflectiveUtils;
import FinalProjectTests.DAL.DalTestUtils;
import org.jfree.data.statistics.DefaultStatisticalCategoryDataset;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class StatisticsHandlerTest {
    private List<AlgorithmProblemResult> experimentResults;
    private Map<String, Map<Integer, Long>>  probToAlgoTotalTime;
    private StatisticsHandler statisticsHandler;

    @Before
    public void setUp() throws Exception {
        //needed for logging
        org.apache.log4j.BasicConfigurator.configure();

        experimentResults = createExpResObj();
        probToAlgoTotalTime = createExpRunningTimes();
        statisticsHandler = new StatisticsHandler(experimentResults, probToAlgoTotalTime);

    }

    private Map<String,Map<Integer,Long>> createExpRunningTimes()
    {
        Map<String,Map<Integer,Long>> res = new HashMap<>();

        Map<Integer,Long> dsa1 = new HashMap<>();
        dsa1.put(0, new Long(200));
        dsa1.put(1, new Long(5));
        dsa1.put(2,  new Long(1));

        res.put("DSA", dsa1);

        Map<Integer,Long> dsa2 = new HashMap<>();
        dsa2.put(0, new Long(100));
        dsa2.put(1, new Long(20));
        dsa2.put(2,  new Long(1));

        res.put("DSA", dsa2);

        return res;


    }

    private List<AlgorithmProblemResult> createExpResObj()
    {
        ProblemAlgorithm pa1 = new ProblemAlgorithm("1", "DSA");
        ProblemAlgorithm pa2 = new ProblemAlgorithm("2", "DSA");

        AlgorithmProblemResult dsa1 =  new AlgorithmProblemResult(pa1);
        AlgorithmProblemResult dsa2 =  new AlgorithmProblemResult(pa2);

        Map<Integer, Double> bestPrice = new HashMap<>();
        bestPrice.put(0, 10.0);
        bestPrice.put(1, 20.0);
        bestPrice.put(2, 30.0);
        dsa1.setTotalGradePerIteration(bestPrice);
        Map<Integer, Double> lowestPrice = new HashMap<>();
        lowestPrice.put(0, 100.0);
        lowestPrice.put(1, 10.0);
        lowestPrice.put(2, 5.0);
        dsa1.setLowestCostForAgentInBestIteration(0, 400.0);
        dsa1.setLowestCostForAgentInBestIteration(1, 10.0);
        dsa1.setLowestCostForAgentInBestIteration(2,5.0);
        dsa1.setHighestCostForAgentInBestIteration(0, 200.0);
        dsa1.setHighestCostForAgentInBestIteration(1, 20.0);
        dsa1.setHighestCostForAgentInBestIteration(2, 40.0);


        Map<Integer, Double> bestPrice2 = new HashMap<>();
        bestPrice2.put(0, 5.0);
        bestPrice2.put(1, 10.0);
        bestPrice2.put(2, 20.0);
        dsa2.setTotalGradePerIteration(bestPrice2);
        Map<Integer, Double> lowestPrice2 = new HashMap<>();
        lowestPrice2.put(0, 1.0);
        lowestPrice2.put(1, 20.0);
        lowestPrice2.put(2, 25.0);
        dsa2.setLowestCostForAgentInBestIteration(0, 200.0);
        dsa2.setLowestCostForAgentInBestIteration(1, 100.0);
        dsa2.setLowestCostForAgentInBestIteration(2,50.0);
        dsa2.setHighestCostForAgentInBestIteration(0, 1000.0);
        dsa2.setHighestCostForAgentInBestIteration(1, 200.0);
        dsa2.setHighestCostForAgentInBestIteration(2, 400.0);

        List<AlgorithmProblemResult> res = new ArrayList<>();
        res.add(dsa1);
        res.add(dsa2);

        return res;
    }

    @After
    public void tearDown() throws Exception {
        experimentResults = new ArrayList<>();
        probToAlgoTotalTime = new HashMap<>();
    }

    @Test
    public void totalConsumption() {
        DefaultStatisticalCategoryDataset data = statisticsHandler.totalConsumption();
        Assert.assertTrue(data.getRowCount() == 1);
        Assert.assertTrue(data.getValue(0,0).equals(7.5));
        Assert.assertTrue(data.getValue(0,1).equals(15.0));
        Assert.assertTrue(data.getValue(0,2).equals(25.0));
        Assert.assertTrue(data.getStdDevValue(0,0).equals(2.9068883707497264));
        Assert.assertTrue(data.getStdDevValue(0,1).equals(5.813776741499453));
        Assert.assertTrue(data.getStdDevValue(0,2).equals(9.219544457292887));
    }

    @Test
    public void lowestAgent() {
        DefaultStatisticalCategoryDataset data = statisticsHandler.lowestAgent();
        Assert.assertTrue(data.getRowCount() == 1);
        Assert.assertTrue(data.getValue(0,0).equals(300.0));
        Assert.assertTrue(data.getValue(0,1).equals(55.0));
        Assert.assertTrue(data.getValue(0,2).equals(27.5));
    }

    @Test
    public void highestAgent() {
        DefaultStatisticalCategoryDataset data = statisticsHandler.highestAgent();
        Assert.assertTrue(data.getRowCount() == 1);
        Assert.assertTrue(data.getValue(0,0).equals(600.0));
        Assert.assertTrue(data.getValue(0,1).equals(110.0));
        Assert.assertTrue(data.getValue(0,2).equals(220.0));
    }


    @Test
    public void averageTime() {
        DefaultStatisticalCategoryDataset data = statisticsHandler.averageTime();
        Assert.assertTrue(data.getRowCount() == 1);
        Assert.assertTrue(data.getValue(0, 0).doubleValue() == 100.0);
        Assert.assertTrue(data.getValue(0, 1).doubleValue() == 20.0);
        Assert.assertTrue(data.getValue(0, 2).doubleValue() == 1.0);
    }

    @Test
    public void messageSendPerIteration() {
        //TODO
    }
}