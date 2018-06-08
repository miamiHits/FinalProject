package FinalProject.DAL;

import FinalProject.BL.DataObjects.Problem;
import FinalProject.BL.ProblemLoadResult;
import FinalProject.Config;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.util.collections.Sets;

import java.util.*;

public class DataAccessControllerTest {

    private DataAccessControllerInterface accessController;

    @Before
    public void setUp() throws Exception
    {
        Config.loadTestConfig();
        JsonLoaderInterface jsonLoader = new JsonLoader(DalTestUtils.JSON_DIR_PATH);
        AlgoLoaderInterface algorithmLoader = new AlgorithmLoader(DalTestUtils.compiledDirBasePath);
        accessController = new DataAccessController(jsonLoader, algorithmLoader);
    }

    @After
    public void tearDown() throws Exception
    {
        org.apache.log4j.BasicConfigurator.configure();
        accessController = null;
    }

    @Test
    public void getProblemsGood() throws Exception
    {
        List<String> lst = Collections.singletonList("dm_7_1_2");
        List<Problem> expected = Collections.singletonList(DalTestUtils.getProblemDm_7_1_2());
        ProblemLoadResult result = accessController.getProblems(lst);
        Assert.assertFalse(result.hasErrors());
        Assert.assertEquals(expected, result.getProblems());
    }



    @Test
    public void getAvailableAlgorithms() throws Exception
    {
        Set<String> expected = Sets.newSet("SHMGM", "DSA", "SA", "DBA");
        List<String> actual = accessController.getAvailableAlgorithms();
        Assert.assertEquals(expected, new HashSet<>(actual));
    }

    @Test
    public void getAlgorithms() throws Exception
    {

    }

    @Test
    public void addAlgorithmToSystem() throws Exception
    {

    }

}