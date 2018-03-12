package FinalProjectTests.DAL;

import FinalProject.BL.DataObjects.*;
import FinalProject.DAL.*;
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
        List<Problem> actual = loader.loadProblems(lst);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void loadProblemsManyProblemsDm_7_1_2Good() throws Exception
    {
        final int NUM_PROBLEMS = 40;
        List<String> lst = new ArrayList<>();
        for (int i = 0; i <= NUM_PROBLEMS; i++)
        {
            lst.add("dm_7_1_2");
        }
        Problem singleResult = DalTestUtils.getProblemDm_7_1_2();
        List<Problem> expected = new ArrayList<>();
        for (int i = 0; i <= NUM_PROBLEMS; i++)
        {
            expected.add(singleResult);
        }
        long start = Calendar.getInstance().getTime().getTime();
        List<Problem> actual = loader.loadProblems(lst);
        long end = Calendar.getInstance().getTime().getTime();
        System.out.println("time: " + (end - start) + " milliseconds");
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void loadProblemsNullPathBad() throws Exception
    {
        List<Problem> actual = loader.loadProblems(null);
        Assert.assertNull(actual);
    }

    @Test
    public void loadProblemsNoFileBad() throws Exception
    {
        List<Problem> actual = loader.loadProblems(Collections.singletonList("some\\path\\to\\nowhere"));
        Assert.assertTrue(actual.isEmpty());
    }

    @Test
    public void loadProblemsBadJsonNoHorizon() throws Exception
    {
        List<Problem> actual = loader.loadProblems(Collections.singletonList("badJson_noHorizon"));
        Assert.assertTrue(actual.isEmpty());
    }

    @Test
    public void loadProblemsBadJsonNoAgents() throws Exception
    {
        List<Problem> actual = loader.loadProblems(Collections.singletonList("badJson_noAgents"));
        Assert.assertTrue(actual.isEmpty());
    }

    @Test
    public void loadProblemsBadJsonNoNeighborsInOneAgent() throws Exception
    {
        List<Problem> actual = loader.loadProblems(Collections.singletonList("badJson_noNeighborsInOneAgent"));
        Assert.assertTrue(actual.isEmpty());
    }

    @Test
    public void getAllProblemNames() throws Exception
    {
        List<String> expectedFileNames = Arrays.asList("bo_135_1_3", "badJson_noNeighborsInOneAgent", "dm_7_1_2", "dm_7_1_3",
                                                       "badJson_noAgents", "badJson_noHorizon", "bo_474_1_3");
        List<String> actualFileName = loader.getAllProblemNames();
        Assert.assertEquals(expectedFileNames.size(), actualFileName.size());
        Assert.assertTrue(expectedFileNames.containsAll(actualFileName));
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

    //*********HELPER METHODS**********
}