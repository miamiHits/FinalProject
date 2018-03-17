package FinalProjectTests.DAL;

import FinalProject.BL.DataObjects.Problem;
import FinalProject.DAL.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

//TODO
public class DataAccessControllerTest {

    private DataAccessControllerInterface accessController;

    @Before
    public void setUp() throws Exception
    {
        JsonLoaderInterface jsonLoader = new JsonLoader("src/test/testResources/jsons".replaceAll("/", Matcher.quoteReplacement(File.separator)));
        AlgoLoaderInterface algorithmLoader = new AlgorithmLoader("src/test/testResources/compiledAlgorithms".replaceAll("/", Matcher.quoteReplacement(File.separator)));
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
        List<Problem> actual = accessController.getProblems(lst);
        Assert.assertEquals(expected, actual);
    }



    @Test
    public void getAvailableAlgorithms() throws Exception
    {
        List<String> expected = Arrays.asList("App");
        List<String> actual = accessController.getAvailableAlgorithms();
        Assert.assertEquals(expected, actual);
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