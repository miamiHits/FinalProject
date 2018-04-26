package FinalProject.BL.DataCollection;

import FinalProject.BL.IterationData.*;
import FinalProject.BL.DataCollection.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DataCollectorTest {
    private DataCollector dataCollector;
    private IterationCollectedData ICD1;
    private IterationCollectedData ICD2;
    private IterationCollectedData ICD3;

    @Before
    public void setUp() throws Exception {
        Map<String, Integer> numOfAgentsInProblems = new HashMap<String, Integer>();
        numOfAgentsInProblems.put("p0", 0);
        numOfAgentsInProblems.put("p1", 2);
        numOfAgentsInProblems.put("p2", 2);
        numOfAgentsInProblems.put("p3", 3);
        numOfAgentsInProblems.put("p4", 3);
        Map<String, double[]> prices = new HashMap<String, double[]>();
        double[] pricePerTick1 = {12.2, 15.5, 44.3, 3, 7 , 77, 12, 5, 78, 11, 12, 23.2};
        double[] pricePerTick2 = {12.2, 15.5, 44.3, 3, 7 , 77, 12, 5, 78, 11, 12, 23.2};
        prices.put("p0", pricePerTick1);
        prices.put("p1", pricePerTick2);
        prices.put("p2", pricePerTick1);
        prices.put("p3", pricePerTick2);
        prices.put("p4", pricePerTick1);
        Set<String> neighborhood1 = new HashSet<String>();
        Set<String> neighborhood2 = new HashSet<String>();
        neighborhood2.add("a0");
        neighborhood1.add("a1");
        dataCollector = new DataCollector(numOfAgentsInProblems, prices);
        double[] powerConsPerTick = new double[]{22.1, 22.3, 55.77, 12.2, 15.5, 44.3, 3, 7 , 77, 12, 5, 78};
        ICD1 = new IterationCollectedData(0,"a0",
            22.33, powerConsPerTick,
        "p4", "algo0", neighborhood1, 22.36, 1, 1); //TODO: set real messageSize, count
        ICD2 = new IterationCollectedData(0,"a0",
                22.33, powerConsPerTick,
                "p1", "algo0", neighborhood1, 25.36, 1, 1); //TODO: set real messageSize, count
        ICD3 = new IterationCollectedData(0,"a1",
                22.33, powerConsPerTick,
                "p1", "algo0", neighborhood2, 20.36, 1, 1); //TODO: set real messageSize, count

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
        Assert.assertTrue(dataCollector.getProbAlgoToItAgentPrice().containsKey(tempPA)
        && dataCollector.getProbAlgoToItAgentPrice().get(tempPA).getAgentsPrices(0).size() == 1);
        dataCollector.addData(ICD3);
        Assert.assertTrue(dataCollector.getProbAlgoToItAgentPrice().containsKey(tempPA));
        Assert.assertTrue(dataCollector.getProbAlgoToItAgentPrice().get(tempPA)
                .getAgentsPrices(0).size() == 2);
    }

    @Test
    public void getNumOfAgentsInProblem() throws Exception {
        Assert.assertTrue(dataCollector.getNumOfAgentsInProblem("p0") == 0);
        Assert.assertTrue(dataCollector.getNumOfAgentsInProblem("p1") == 2);
        Assert.assertTrue(dataCollector.getNumOfAgentsInProblem("p2") == 2);
        Assert.assertTrue(dataCollector.getNumOfAgentsInProblem("p3") == 3);
    }

}