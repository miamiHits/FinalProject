package FinalProject.BL.DataCollection;

import FinalProject.BL.Agents.SHMGM;
import FinalProjectTests.BL.Agents.ReflectiveUtils;
import FinalProjectTests.DAL.DalTestUtils;
import org.junit.After;
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
        dsa1.put(1, new Long(5));
        dsa1.put(2,  new Long(1));

        res.put("DSA", dsa1);

        return res;


    }

    private List<AlgorithmProblemResult> createExpResObj()
    {
        ProblemAlgorithm pa1 = new ProblemAlgorithm("1", "DSA");
        ProblemAlgorithm pa2 = new ProblemAlgorithm("2", "DSA");

        AlgorithmProblemResult dsa1 =  new AlgorithmProblemResult(pa1);
        AlgorithmProblemResult dsa2 =  new AlgorithmProblemResult(pa2);

        Map<Integer, Double> bestPrice = new HashMap<>();
        bestPrice.put(1, 20.0);
        bestPrice.put(2, 30.0);
        dsa1.setTotalGradePerIteration(bestPrice);
        Map<Integer, Double> lowestPrice = new HashMap<>();
        lowestPrice.put(1, 10.0);
        lowestPrice.put(2, 5.0);
        dsa1.setLowestCostForAgentInBestIteration(1, 10.0);
        dsa1.setLowestCostForAgentInBestIteration(2,5.0);
        dsa1.setHighestCostForAgentInBestIteration(1, 20.0);
        dsa1.setHighestCostForAgentInBestIteration(2, 40.0);


        Map<Integer, Double> bestPrice2 = new HashMap<>();
        bestPrice2.put(1, 10.0);
        bestPrice2.put(2, 20.0);
        dsa2.setTotalGradePerIteration(bestPrice2);
        Map<Integer, Double> lowestPrice2 = new HashMap<>();
        lowestPrice2.put(1, 20.0);
        lowestPrice2.put(2, 25.0);
        dsa2.setLowestCostForAgentInBestIteration(1, 100.0);
        dsa2.setLowestCostForAgentInBestIteration(2,50.0);
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
    }

    @Test
    public void totalConsumption() {


    }

    @Test
    public void lowestAgent() {
    }

    @Test
    public void highestAgent() {
    }

    @Test
    public void calculateSD() {
    }

    @Test
    public void averageTime() {
    }

    @Test
    public void messageSendPerIteration() {
    }
}