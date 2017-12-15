package FinalProject.DAL;

import FinalProject.BL.Problems.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

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
    public void loadProblemsDm_7_1_2Good() throws Exception
    {
        List<String> lst = Collections.singletonList("dm_7_1_2");
        List<Problem> expected = Collections.singletonList(getProblemDm_7_1_2());
        List<Problem> actual = loader.loadProblems(lst);
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
                                                       "badJson_noAgents", "badJson_noHorizon");
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

    //*********HELPER METHODS**********

    private Problem getProblemDm_7_1_2()
    {
        Map<Integer, List<Device>> deviceDict = loader.getDeviceDict();
        List<AgentData> allHomes = createDm_7_1_2AllHomes(deviceDict);
        double[] priceSchema = {0.198, 0.198, 0.198, 0.198, 0.225, 0.225, 0.249, 0.849, 0.849, 0.225, 0.225, 0.198};
        return new Problem("dm_7_1_2", deviceDict, allHomes, 12, 60, priceSchema);
    }

    private List<AgentData> createDm_7_1_2AllHomes(Map<Integer, List<Device>> deviceDict)
    {
        double[][] backgroundLoadsArr = {
                new double[] {0.23, 0.28, 0.08, 0.23, 0.19, 0.05, 0.15, 0.11, 0.24, 0.19, 0.18, 0.02},
                new double[] {0.23, 0.09, 0.05, 0.1, 0.1, 0.03, 0.29, 0.01, 0.23, 0.2, 0.23, 0.04},
                new double[] {0.12, 0.21, 0.04, 0.05, 0.29, 0.15, 0.2, 0.09, 0.16, 0.25, 0.14, 0.01},
                new double[] {0.06, 0.16, 0.13, 0.23, 0.02, 0.13, 0.27, 0.29, 0.04, 0.12, 0.15, 0.13},
                new double[] {0.23, 0.21, 0.23, 0.01, 0.09, 0.27, 0.08, 0.1, 0.02, 0.18, 0.11, 0.07},
                new double[] {0.11, 0.1, 0.25, 0.15, 0.15, 0.11, 0.25, 0.07, 0.06, 0.26, 0.22, 0.08},
                new double[] {0.07, 0.29, 0.22, 0.25, 0.02, 0.14, 0.23, 0.02, 0.15, 0.2, 0.01, 0.28}
        };
        String[][] rulesArr = {
                new String[] {"1 Tesla_S charge leq 59 before 3", "0 Tesla_S charge geq 0", "0 Tesla_S charge leq 100", "1 GE_WSM2420D3WW_wash laundry_wash eq 60 before 6", "0 GE_WSM2420D3WW_wash laundry_wash geq 0", "0 GE_WSM2420D3WW_wash laundry_wash leq 60"},
                new String[] {"1 GE_WSM2420D3WW_wash laundry_wash eq 60 after 8", "0 GE_WSM2420D3WW_wash laundry_wash geq 0", "0 GE_WSM2420D3WW_wash laundry_wash leq 60", "1 Tesla_S charge gt 80 after 1", "0 Tesla_S charge geq 0", "0 Tesla_S charge leq 100"},
                new String[] {"1 room temperature_heat lt 23 after 4", "0 room temperature_heat geq 8", "0 room temperature_heat leq 35", "1 Kenmore_665.13242K900 dish_wash eq 60 after 2", "0 Kenmore_665.13242K900 dish_wash geq 0", "0 Kenmore_665.13242K900 dish_wash leq 60"},
                new String[] {"1 Tesla_S charge gt 66 before 5", "0 Tesla_S charge geq 0", "0 Tesla_S charge leq 100", "1 Kenmore_790.91312013 bake eq 60 before 11", "0 Kenmore_790.91312013 bake geq 0", "0 Kenmore_790.91312013 bake leq 60"},
                new String[] {"1 water_tank water_temp geq 53 after 7", "0 water_tank water_temp geq 37", "0 water_tank water_temp leq 78", "1 room cleanliness gt 75 after 4", "0 room cleanliness geq 0", "0 room cleanliness leq 100", "0 Roomba_880 charge geq 0", "0 Roomba_880 charge leq 100"},
                new String[] {"1 Tesla_S charge geq 71 after 7", "0 Tesla_S charge geq 0", "0 Tesla_S charge leq 100", "1 GE_WSM2420D3WW_dry laundry_dry eq 60 before 10", "0 GE_WSM2420D3WW_dry laundry_dry geq 0", "0 GE_WSM2420D3WW_dry laundry_dry leq 60"},
                new String[] {"1 GE_WSM2420D3WW_dry laundry_dry eq 60 after 8", "0 GE_WSM2420D3WW_dry laundry_dry geq 0", "0 GE_WSM2420D3WW_dry laundry_dry leq 60", "1 GE_WSM2420D3WW_wash laundry_wash eq 60 before 3", "0 GE_WSM2420D3WW_wash laundry_wash geq 0", "0 GE_WSM2420D3WW_wash laundry_wash leq 60"}

        };
        String[][] actArr = {
                new String[] {"Tesla_S","GE_WSM2420D3WW_wash"},
                new String[] {"GE_WSM2420D3WW_wash", "Tesla_S"},
                new String[] {"Dyson_AM09", "Kenmore_665.13242K900"},
                new String[] {"Tesla_S", "Kenmore_790.91312013"},
                new String[] {"Rheem_XE40M12ST45U1", "Roomba_880"},
                new String[] {"Tesla_S", "GE_WSM2420D3WW_dry"},
                new String[] {"GE_WSM2420D3WW_dry", "GE_WSM2420D3WW_wash"}
        };
        String[][] sensArr = {
                new String[] {"Tesla_S_battery", "GE_WSM2420D3WW_wash_sensor"},
                new String[] {"GE_WSM2420D3WW_wash_sensor", "Tesla_S_battery"},
                new String[] {"thermostat_heat", "Kenmore_665_sensor"},
                new String[] {"Tesla_S_battery", "Kenmore_790_sensor"},
                new String[] {"water_heat_sensor", "dust_sensor", "iRobot_651_battery"},
                new String[] {"Tesla_S_battery", "GE_WSM2420D3WW_dry_sensor"},
                new String[] {"GE_WSM2420D3WW_dry_sensor", "GE_WSM2420D3WW_wash_sensor"}
        };
        int[] htArr = {0, 0, 0, 1, 1, 2, 2};

        return createAllHomesFromArrays(deviceDict, backgroundLoadsArr, rulesArr, actArr, sensArr, htArr);
    }

    private List<AgentData> createAllHomesFromArrays(Map<Integer, List<Device>> deviceDict,
                                                     double[][] backgroundLoadsArr, String[][] rulesArr,
                                                     String[][] actArr, String[][] sensArr, int[] htArr)
    {
        List<AgentData> allHomes = new ArrayList<>(backgroundLoadsArr.length);
        for (int i = 0; i < backgroundLoadsArr.length; i++)
        {
            allHomes.add(new AgentData("h" + (i + 1)));
        }
        addAllOtherHousesAsNeighbors(allHomes);

        for (int i = 0; i < backgroundLoadsArr.length; i++)
        {
            AgentData currHome = allHomes.get(i);
            currHome.setHouseType(htArr[i]);
            currHome.setBackgroundLoad(backgroundLoadsArr[i]);
            final List<Device> devices = deviceDict.get(currHome.getHouseType());
            addRulesFromStrArr(currHome, devices, rulesArr[i]);
            addActuatorsFromStrArr(currHome, devices, actArr[i]);
            addSensorsFromStrArr(currHome, devices, sensArr[i]);
        }
        return allHomes;
    }

    private void addActuatorsFromStrArr(AgentData home, List<Device> devices,
                                        String[] actsStrArr)
    {
        home.setActuators(new ArrayList<>(actsStrArr.length));
        for (String actStr : actsStrArr)
        {
            for (Device dev : devices)
            {
                if (dev.getName().equals(actStr))
                {
                    home.getActuators().add((Actuator) dev);
                    break;
                }
            }
        }
    }

    private void addSensorsFromStrArr(AgentData home, List<Device> devices,
                                        String[] sensorsStrArr)
    {
        home.setSensors(new ArrayList<>(sensorsStrArr.length));
        for (String actStr : sensorsStrArr)
        {
            for (Device dev : devices)
            {
                if (dev.getName().equals(actStr))
                {
                    home.getSensors().add((Sensor) dev);
                    break;
                }
            }
        }
    }

    private void addRulesFromStrArr(AgentData home, List<Device> allDevices, String[] rulesArr)
    {
        home.setRules(new ArrayList<>(rulesArr.length));
        for (String ruleStr : rulesArr)
        {
            home.getRules().add(new Rule(ruleStr, allDevices));
        }
    }

    private void addAllOtherHousesAsNeighbors(List<AgentData> allHomes)
    {
        for (int currNum = 0; currNum < allHomes.size(); currNum++)
        {
            AgentData currHome = allHomes.get(currNum);
            currHome.setNeighbors(new ArrayList<>(allHomes.size() - 1));
            for (int i = 0; i < allHomes.size(); i++)
            {
                if (i != currNum)
                {
                    currHome.getNeighbors().add(allHomes.get(i));
                }
            }
        }
    }

}