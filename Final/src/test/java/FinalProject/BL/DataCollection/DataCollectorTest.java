package FinalProject.BL.DataCollection;

import FinalProject.BL.IterationData.IterationCollectedData;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class DataCollectorTest {
    private DataCollector dataCollector;
    private IterationCollectedData ICD1;
    private IterationCollectedData ICD2;
    private IterationCollectedData ICD3;

    @Before
    public void setUp() throws Exception {
        Map<String, Integer> numOfAgentsInProblems = new HashMap<String, Integer>();
        numOfAgentsInProblems.put("p0", 0);
        numOfAgentsInProblems.put("p1", 1);
        numOfAgentsInProblems.put("p2", 2);
        numOfAgentsInProblems.put("p3", 3);
        dataCollector = new DataCollector(numOfAgentsInProblems);
        double[] powerConsPerDevice = new double[]{22.1, 22.3, 55.77};
        ICD1 = new IterationCollectedData(0,"a0",
            22.33, 222.1, powerConsPerDevice,
        "p4", "algo0");
        ICD2 = new IterationCollectedData(0,"a0",
                22.33, 222.1, powerConsPerDevice,
                "p1", "algo0");
        ICD3 = new IterationCollectedData(0,"a1",
                22.33, 222.1, powerConsPerDevice,
                "p1", "algo0");

    }

    @Test
    public void addDataNewProblem() throws Exception {
        ProblemAlgorithm tempPA = new ProblemAlgorithm(ICD1.getProblemId(), ICD1.getAlgorithm());
        Assert.assertFalse(dataCollector.getProbAlgoToItAgentPrice().containsKey(tempPA));
        dataCollector.addData(ICD1);
        Assert.assertTrue(dataCollector.getProbAlgoToItAgentPrice().containsKey(tempPA));
    }

    @Test
    public void addDataExistingProblem() throws Exception {
        ProblemAlgorithm tempPA = new ProblemAlgorithm(ICD2.getProblemId(), ICD2.getAlgorithm());
        Assert.assertFalse(dataCollector.getProbAlgoToItAgentPrice().containsKey(tempPA));
        dataCollector.addData(ICD2);
        Assert.assertTrue(dataCollector.getProbAlgoToItAgentPrice().containsKey(tempPA));
        Assert.assertTrue(dataCollector.getProbAlgoToItAgentPrice().get(tempPA)
                .getAgentsPrices(0).size() == 1);
        dataCollector.addData(ICD3);
        Assert.assertTrue(dataCollector.getProbAlgoToItAgentPrice().containsKey(tempPA));
        Assert.assertTrue(dataCollector.getProbAlgoToItAgentPrice().get(tempPA)
                .getAgentsPrices(0).size() == 2);
    }

    @Test
    public void getNumOfAgentsInProblem() throws Exception {
        Assert.assertTrue(dataCollector.getNumOfAgentsInProblem("p0") == 0);
        Assert.assertTrue(dataCollector.getNumOfAgentsInProblem("p1") == 1);
        Assert.assertTrue(dataCollector.getNumOfAgentsInProblem("p2") == 2);
        Assert.assertTrue(dataCollector.getNumOfAgentsInProblem("p3") == 3);
    }

}