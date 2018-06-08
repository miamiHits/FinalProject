package FinalProject.DAL;

import FinalProject.BL.DataObjects.*;
import FinalProject.BL.ProblemLoadResult;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

public class JsonLoaderTest {

    private JsonLoader loader;
    private final static String dirPath = DalTestUtils.JSON_DIR_PATH;

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
    public void loadProblemsDm_7_1_2Good() throws Exception
    {
        List<String> lst = Collections.singletonList("dm_7_1_2");
        Problem dm_7_1_2 = DalTestUtils.getProblemDm_7_1_2();
        List<Problem> expected = Collections.singletonList(dm_7_1_2);
        ProblemLoadResult result = loader.loadProblems(lst);
        Assert.assertFalse(result.hasErrors());
        Assert.assertEquals(expected, result.getProblems());
    }

    @Test
    public void loadProblemsManyProblemsDm_7_1_2Good() throws Exception
    {
        final int NUM_PROBLEMS = 40;
        List<String> lst = new ArrayList<>();
        for (int i = 0; i <= NUM_PROBLEMS; i++) {
            lst.add("dm_7_1_2");
        }
        List<Problem> expected = new ArrayList<>();
        for (int i = 0; i <= NUM_PROBLEMS; i++) {
            expected.add(DalTestUtils.getProblemDm_7_1_2());
        }
        ProblemLoadResult result = loader.loadProblems(lst);
        Assert.assertFalse(result.hasErrors());
        Assert.assertEquals(expected, result.getProblems());
    }

    @Test
    public void loadProblemsNullPathBad() throws Exception
    {
        ProblemLoadResult result = loader.loadProblems(null);
        Assert.assertNull(result);
    }

    @Test
    public void loadProblemsNoFileBad() throws Exception
    {
        ProblemLoadResult result = loader.loadProblems(Collections.singletonList("some\\path\\to\\nowhere"));
        Assert.assertTrue(result.hasErrors());
        Assert.assertEquals(1, result.getErrors().size());
        Assert.assertTrue(result.getErrors().get(0).contains("IO Exception"));
        Assert.assertTrue(result.getProblems().isEmpty());
    }

    @Test
    public void loadProblemsBadJsonNoHorizon() throws Exception
    {
        ProblemLoadResult result = loader.loadProblems(Collections.singletonList("badJson_noHorizon"));
        Assert.assertTrue(result.hasErrors());
        Assert.assertEquals(1, result.getErrors().size());
        Assert.assertTrue(result.getErrors().get(0).contains("IO Exception"));
        Assert.assertTrue(result.getProblems().isEmpty());
    }

    @Test
    public void loadProblemsBadJsonNoAgents() throws Exception
    {
        ProblemLoadResult result = loader.loadProblems(Collections.singletonList("badJson_noAgents"));
        Assert.assertTrue(result.hasErrors());
        Assert.assertEquals(1, result.getErrors().size());
        Assert.assertTrue(result.getProblems().isEmpty());
    }

    @Test
    public void loadProblemsBadJsonNoNeighborsInOneAgent() throws Exception
    {
        ProblemLoadResult result = loader.loadProblems(Collections.singletonList("badJson_noNeighborsInOneAgent"));
        Assert.assertTrue(result.hasErrors());
        Assert.assertEquals(1, result.getErrors().size());
        Assert.assertTrue(result.getProblems().isEmpty());
    }


    @Test
    public void getAllProblemNames() throws Exception
    {
        List<String> expected = Arrays.asList("bo_135_1_3", "bo_474_1_3", "dm_2_1_3", "dm_7_1_2", "dm_7_1_3", "dm_7_1_4", "dm_7_1_5", "dm_7_1_6",
                "badJson_-1_noNeighborsInOneAgent", "badJson_-1_noAgents", "badJson_-1_noHorizon");
        List<String> actualFileName = loader.getAllProblemNames();
        Assert.assertEquals(expected.size(), actualFileName.size());
        Assert.assertEquals(new HashSet<>(expected), new HashSet<>(actualFileName));
    }

    @Test
    public void getAllDevicesMapSizeGood() throws Exception
    {
        Map<Integer, List<Device>> map = loader.loadDevices();
        Assert.assertEquals(map.size(), 3);
    }

    @Test
    public void getAllDevicesEachListInMapSizeGood() throws Exception
    {
        Map<Integer, List<Device>> map = loader.loadDevices();
        Assert.assertEquals(map.get(0).size(), map.get(1).size());
        Assert.assertEquals(map.get(0).size(), map.get(2).size());
    }

    @Test
    public void getAllDevicesSameDeviceNameGood() throws Exception
    {
        Map<Integer, List<Device>> map = loader.loadDevices();
        List<String> ht0DevNames = map.get(0).stream()
                .map(Device::getName)
                .collect(Collectors.toList());
        List<String> ht1DevNames = map.get(1).stream()
                .map(Device::getName)
                .collect(Collectors.toList());
        List<String> ht2DevNames = map.get(2).stream()
                .map(Device::getName)
                .collect(Collectors.toList());

        Assert.assertEquals(ht0DevNames, ht1DevNames);
        Assert.assertEquals(ht0DevNames, ht2DevNames);
    }

    @Test
    public void getAllDevicesDistinctDeviceEachListGood() throws Exception
    {
        Map<Integer, List<Device>> map = loader.loadDevices();
        long ht0DistinctDevs = map.get(0).stream()
                .distinct()
                .count();
        long ht1DistinctDevs = map.get(1).stream()
                .distinct()
                .count();
        long ht2DistinctDevs = map.get(2).stream()
                .distinct()
                .count();

        Assert.assertEquals(ht0DistinctDevs, map.get(0).size());
        Assert.assertEquals(ht1DistinctDevs, map.get(1).size());
        Assert.assertEquals(ht2DistinctDevs, map.get(2).size());
    }
}