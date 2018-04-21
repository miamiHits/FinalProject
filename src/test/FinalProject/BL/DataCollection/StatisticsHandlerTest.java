package FinalProject.BL.DataCollection;

import FinalProject.BL.Agents.SHMGM;
import FinalProjectTests.BL.Agents.ReflectiveUtils;
import FinalProjectTests.DAL.DalTestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class StatisticsHandlerTest {
    private List<AlgorithmProblemResult> experimentResults = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        //needed for logging
        org.apache.log4j.BasicConfigurator.configure();

        experimentResults = createExpResObj();

    }

    private List<AlgorithmProblemResult> createExpResObj()
    {
        ProblemAlgorithm pa1 = new ProblemAlgorithm("1", "DSA");
        AlgorithmProblemResult dsa1 =  new AlgorithmProblemResult(pa1);
            return null;
    }

    @After
    public void tearDown() throws Exception {

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