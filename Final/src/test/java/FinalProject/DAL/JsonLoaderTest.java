package FinalProject.DAL;

import FinalProject.BL.Problems.Device;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class JsonLoaderTest {

    private JsonLoader loader;
    private final static String dirPath = "src\\test\\testResources\\jsons";

    @Before
    public void setUp() throws Exception
    {
        org.apache.log4j.BasicConfigurator.configure();
        loader = new JsonLoader(dirPath);
    }

    @After
    public void tearDown() throws Exception
    {
        loader = null;
    }

    @Test
    public void loadProblems() throws Exception
    {

    }

    @Test
    public void getAllProblemNames() throws Exception
    {
        List<String> expectedFileNames = Arrays.asList("bo_135_1_3", "bo_474_1_3", "dm_7_1_2", "dm_7_1_3");
        List<String> actualFileName = loader.getAllProblemNames();
        Assert.assertEquals(expectedFileNames.size(), actualFileName.size());
        Assert.assertTrue(expectedFileNames.containsAll(actualFileName));
    }

    @Test
    public void getAllDevices() throws Exception
    {
        //TODO: improve this
        Map<Integer, List<Device>> map = loader.loadDevices();
        Assert.assertEquals(map.size(), 3);
        Assert.assertEquals(map.get(0).size(), map.get(1).size());
        Assert.assertEquals(map.get(0).size(), map.get(2).size());
    }

}