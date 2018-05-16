package FinalProject.BL.Agents;

import FinalProject.BL.DataCollection.PowerConsumptionUtils;
import FinalProject.BL.DataObjects.Problem;
import FinalProject.BL.IterationData.AgentIterationData;
import FinalProject.DAL.DalTestUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

public class SATest {

    private Problem dm_7_1_2;
    private SA sa;
    private SmartHomeAgent agent;

    @Before
    public void setUp() throws Exception {
        //needed for logging
        org.apache.log4j.BasicConfigurator.configure();

        //create a problem obj
        dm_7_1_2 = DalTestUtils.getProblemDm_7_1_2();
        agent = ReflectiveUtils.initSmartHomeAgentForTest(dm_7_1_2);
        sa = new SA();
        sa.initializeBehaviourWithAgent(agent);
    }

    @After
    public void tearDown() throws Exception {
        dm_7_1_2 = null;
        agent = null;
    }

    @Test
    public void generateScheduleForPropTest() throws NoSuchFieldException, IllegalAccessException {
        sa.currentNumberOfIter = 1;
        agent.setZEROIteration(false);
        Map<PropertyWithData, List<Set<Integer>>> propToSubsetsMap = ReflectiveUtils.getFieldValue(sa, "propToSubsetsMap");
        Map<PropertyWithData, Map<String,Integer>> propToSensorsToChargeMap = ReflectiveUtils.getFieldValue(sa, "propToSensorsToChargeMap");

        Assert.assertTrue(propToSensorsToChargeMap.isEmpty());
        Assert.assertTrue(propToSubsetsMap.isEmpty());

        PropertyWithData prop = getPropertyWithData();
        Map<String,Integer> sensorsToCharge = new HashMap<>();
        sensorsToCharge.put("bla", 100);
        sa.generateScheduleForProp(prop, 10, sensorsToCharge, false);

        Assert.assertEquals(1, propToSubsetsMap.size());
        Assert.assertEquals(1, propToSensorsToChargeMap.size());
        Assert.assertTrue(propToSensorsToChargeMap.containsValue(sensorsToCharge));
        Assert.assertTrue(propToSubsetsMap.containsKey(prop));
        Assert.assertTrue(propToSensorsToChargeMap.containsKey(prop));
    }

    private PropertyWithData getPropertyWithData() {
        sa.getHelper().buildNewPropertyData(dm_7_1_2.getAgentsData().get(0).getRules().get(0), false);
        PropertyWithData prop = sa.getHelper().getAllProperties().get(0);
        prop.setName("Test Prop");
        prop.setActuator(dm_7_1_2.getAgentsData().get(0).getActuators().get(0));
        return prop;
    }

    @Test
    public void pickRandomSubsetForPropTestEmpty() throws NoSuchFieldException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        PropertyWithData prop = getPropertyWithData();
        Map<PropertyWithData, List<Set<Integer>>> propToSubsetsMap = ReflectiveUtils.getFieldValue(sa, "propToSubsetsMap");
        propToSubsetsMap.put(prop, new ArrayList<>());

        Set<Integer> actual = (Set<Integer>) ReflectiveUtils.invokeMethod(sa, "pickRandomSubsetForProp", prop);
        Assert.assertTrue(actual.isEmpty());
    }

    @Test
    public void pickRandomSubsetForPropTestNotEmpty() throws NoSuchFieldException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        PropertyWithData prop = getPropertyWithData();
        Map<PropertyWithData, List<Set<Integer>>> propToSubsetsMap = ReflectiveUtils.getFieldValue(sa, "propToSubsetsMap");
        Set<Integer> set = new HashSet<>();
        set.add(1);
        set.add(2);
        set.add(3);

        ArrayList<Set<Integer>> list = new ArrayList<>();
        list.add(set);
        propToSubsetsMap.put(prop, list);

        Set<Integer> actual = (Set<Integer>) ReflectiveUtils.invokeMethod(sa, "pickRandomSubsetForProp", prop);
        Assert.assertEquals(actual, set);
    }

    @Test
    public void countIterationCommunicationTestIterNonZeroMsgSizeGood() throws NoSuchFieldException, IllegalAccessException {
        AgentIterationData iterationData = new AgentIterationData(10,
                "Test", 1, new double[12]);
        ReflectiveUtils.setFieldValue(sa, "agentIterationData", iterationData);
        sa.countIterationCommunication();
        Assert.assertEquals(1734, agent.getIterationMessageSize());
    }

    @Test
    public void countIterationCommunicationTestIterNonZeroMsgCountGood() throws NoSuchFieldException, IllegalAccessException {
        AgentIterationData iterationData = new AgentIterationData(10,
                "Test", 1, new double[12]);
        ReflectiveUtils.setFieldValue(sa, "agentIterationData", iterationData);
        sa.countIterationCommunication();
        Assert.assertEquals(7, agent.getIterationMessageCount());
    }

    @Test
    public void calcProbabilityToTakeNewSchedTestIter1() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        final int TOTAL_NUM_OF_ITER = 100;
        final int CURR_NUM_OF_ITER = 1;

        agent.getAgentData().setNumOfIterations(TOTAL_NUM_OF_ITER);
        sa.currentNumberOfIter = CURR_NUM_OF_ITER;

        float expected = 1 -  (1f/100);
        float actual = (float) ReflectiveUtils.invokeMethod(sa, "calcProbabilityToTakeNewSched");
        Assert.assertEquals(expected, actual, 0);
    }

    @Test
    public void calcProbabilityToTakeNewSchedTestIter10() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        final int TOTAL_NUM_OF_ITER = 100;
        final int CURR_NUM_OF_ITER = 10;

        agent.getAgentData().setNumOfIterations(TOTAL_NUM_OF_ITER);
        sa.currentNumberOfIter = CURR_NUM_OF_ITER;

        float expected = 1 -  (10f/100);
        float actual = (float) ReflectiveUtils.invokeMethod(sa, "calcProbabilityToTakeNewSched");
        Assert.assertEquals(expected, actual, 0);
    }

    @Test
    public void calcProbabilityToTakeNewSchedTestNegNumOfIters() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        final int TOTAL_NUM_OF_ITER = -1;
        final int CURR_NUM_OF_ITER = 10;

        agent.getAgentData().setNumOfIterations(TOTAL_NUM_OF_ITER);
        sa.currentNumberOfIter = CURR_NUM_OF_ITER;

        float expected = -1;
        float actual = (float) ReflectiveUtils.invokeMethod(sa, "calcProbabilityToTakeNewSched");
        Assert.assertEquals(expected, actual, 0);
    }

    @Test
    public void calcImproveOptionGradeTestAllOnes() {
        final int NUM_OF_NEIGHBOURS = 6;
        double[] powerCons = {1,1,1,1,1,1,1,1,1,1,1,1};
        List<double[]> allScheds = new ArrayList<>();

        for (int i = 0; i < NUM_OF_NEIGHBOURS; i++) {
            allScheds.add(powerCons);
        }
        List<AgentIterationData> iterDatas = allScheds.stream()
                .map(sched -> new AgentIterationData(0, "", 0, powerCons))
                .collect(Collectors.toList());
        agent.setMyNeighborsShed(iterDatas);
        double expected = sa.calcCsum(powerCons);
        allScheds.add(powerCons);
        expected += PowerConsumptionUtils.calculateEPeak(allScheds);
        double res = sa.calcImproveOptionGrade(powerCons, allScheds);
        Assert.assertEquals(expected, res, 0.0);
    }

    @Test
    public void calcImproveOptionGradeTestAllZeros() {
        final int NUM_OF_NEIGHBOURS = 6;
        double[] powerCons = {0,0,0,0,0,0,0,0,0,0,0,0};
        List<double[]> allScheds = new ArrayList<>();
        for (int i = 0; i < NUM_OF_NEIGHBOURS + 1; i++) {
            allScheds.add(powerCons);
        }
        double expected = 0;
        double res = sa.calcImproveOptionGrade(powerCons, allScheds);
        Assert.assertEquals(expected, res, 0.0);
    }

    @Test
    public void equalsTestSameInstance() {
        Assert.assertTrue(sa.equals(sa));
    }

    @Test
    public void equalsTestNotEquals() {
        SA copy = sa.cloneBehaviour();
        copy.setAgent(null);
        Assert.assertFalse(sa.equals(copy));
    }
}