package FinalProjectTests.BL.DataCollection;

import FinalProject.BL.DataCollection.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class AlgorithmProblemResultTest {

    private AlgorithmProblemResult result = new AlgorithmProblemResult(new ProblemAlgorithm("p0", "algo0"));

    @Before
    public void setUp() throws Exception {
        result = new AlgorithmProblemResult(new ProblemAlgorithm("p0", "algo0"));
    }

        @Test
    public void setLowestCostForAgentInBestIteration() throws Exception {
        result.setLowestCostForAgentInBestIterationAgentName("h1@213.213213.21/JADE", 1);
        Assert.assertTrue(result.getLowestCostForAgentInBestIterationAgentName().get(1).equals("h1"));
    }

    @Test
    public void setHighestCostForAgentInBestIterationAgentName() throws Exception {
        result.setLowestCostForAgentInBestIterationAgentName("h2@213.213213.21/JADE", 1);
        Assert.assertTrue(result.getLowestCostForAgentInBestIterationAgentName().get(1).equals("h2"));
    }

    @Test
    public void setTotalGradeToIter() throws Exception {
        result.setTotalGradeToIter(0, 5000.12);
        Assert.assertTrue(result.getTotalGradePerIteration().get(0) == 5000.12);
    }

}