package FinalProject.BL.Agents;

import FinalProject.BL.DataObjects.*;
import FinalProject.BL.IterationData.AgentIterationData;
import FinalProject.DAL.DalTestUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

public class SmartHomeAgentBehaviourTest {

    private SmartHomeAgent agent;
    private Problem dm_7_1_2;
    private SmartHomeAgentBehaviour smab;
    private List<PropertyWithData> props;


    @Before
    public void setUp() throws Exception {
        //needed for logging
        org.apache.log4j.BasicConfigurator.configure();

        //create a problem obj
        dm_7_1_2 = DalTestUtils.getProblemDm_7_1_2();
        agent = ReflectiveUtils.initSmartHomeAgentForTest(dm_7_1_2);
        smab = new DSA(agent);
        smab.initializeBehaviourWithAgent(agent);

        props = new ArrayList<>(2);
        AlgoTestUtils.makeProp(props);
    }

    @After
    public void tearDown() throws Exception {
        dm_7_1_2 = null;
        smab = null;
        agent = null;
        props = null;
    }

    @Test
    public void buildScheduleFromScratch() {
        smab.buildScheduleFromScratch();
        Assert.assertTrue(smab.getHelper().getAllProperties().size()==2);
        Assert.assertTrue(smab.getHelper().getDeviceToTicks().size()==2);
        Assert.assertTrue(smab.getHelper().getAllProperties().get(0).getSensor().getCurrentState()> smab.getHelper().getAllProperties().get(0).getMin());
        Assert.assertTrue(smab.getHelper().getAllProperties().get(0).getSensor().getCurrentState()<= smab.getHelper().getAllProperties().get(0).getMax());
        Assert.assertTrue(smab.getHelper().getAllProperties().get(1).getSensor().getCurrentState()> smab.getHelper().getAllProperties().get(1).getMin());
        Assert.assertTrue(smab.getHelper().getAllProperties().get(1).getSensor().getCurrentState()<= smab.getHelper().getAllProperties().get(1).getMax());
        Assert.assertTrue(smab.tempBestPriceConsumption > 1);
        for (Double d: smab.iterationPowerConsumption) {
            Assert.assertTrue(d > 0);
        }

        for(Map.Entry<Actuator, Map<Action, List<Integer>>> entry: smab.getHelper().getDeviceToTicks().entrySet()) {
            for (Map.Entry<Action, List<Integer>> res: entry.getValue().entrySet()) {
                if (entry.getKey().getName().equals("GE_WSM2420D3WW_wash")){ // need to work only 1 Tick.
                    Assert.assertTrue(res.getValue().size()==1);
                }
                else if (entry.getKey().getName().equals("Tesla_S")) // need to work 3 Ticks.
                {
                    Assert.assertTrue(res.getValue().size()==2 || res.getValue().size()==3);
                    Assert.assertTrue(res.getValue().contains(0) || res.getValue().contains(1) || res.getValue().contains(2));

                }

            }
        }
    }

    @Test
    public void calcHowManyTicksNeedToChargeTest() {
        PropertyWithData prop = new PropertyWithData();
        prop.setDeltaWhenWork(10);
        prop.setMax(100);
        prop.setMin(0);
        prop.setName("test prop");
        Sensor sens = new Sensor("bla", "bla", "bla",
                0.0, new ArrayList<>());
        sens.setCurrentState(-20);
        prop.setSensor(sens);
        smab.getHelper().getAllProperties().add(prop);

        int res = smab.calcHowManyTicksNeedToCharge(prop.getName(), prop.getDeltaWhenWork(), 3);
        int expected = 2;

        Assert.assertEquals(expected, res);

    }

    @Test
    public void drawRandomNumIsInRangeTest() {
        int low = 0;
        int high = 100;
        int res = smab.drawRandomNum(low, high);
        Assert.assertTrue(res >= low);
        Assert.assertTrue(res <= high);
    }

    @Test
    public void updateTotalsTestNoTicks() {
        PropertyWithData prop = new PropertyWithData();
        Actuator act = new Actuator("act", "subtype", "location", new ArrayList<>());
        prop.setDeltaWhenWork(10);
        prop.setActuator(act);
        final double sensInitState = 0.0;
        Sensor sens = new Sensor("name", "subtype", "location",
                sensInitState, new ArrayList<>());
        prop.setSensor(sens);
        prop.setMax(100);
        smab.iterationPowerConsumption = new double[dm_7_1_2.getHorizon()];
        double[] initConsArr = Arrays.copyOf(smab.iterationPowerConsumption, dm_7_1_2.getHorizon());

        //empty ticks lst, empty sensors map
        smab.updateTotals(prop, new ArrayList<>(), new HashMap<>());

        //nothing should change:
        Assert.assertEquals(sensInitState, sens.getCurrentState(), 0);
        assertArrayEquals(initConsArr, smab.iterationPowerConsumption, 0);
    }

    @Test
    public void updateTotalsTestNoSensorsSensPassMaxShouldEqualMax() {
        PropertyWithData prop = new PropertyWithData();
        Actuator act = new Actuator("act", "subtype", "location", new ArrayList<>());
        prop.setActuator(act);
        prop.setDeltaWhenWork(11);
        prop.setPowerConsumedInWork(10);
        final double sensInitState = 0.0;
        Sensor sens = new Sensor("sens name", "subtype", "location",
                sensInitState, new ArrayList<>());
        prop.setSensor(sens);
        prop.setMax(10);
        List<Integer> ticks = Arrays.asList(1, 2, 3);


        //empty ticks lst, empty sensors map
        smab.iterationPowerConsumption = new double[dm_7_1_2.getHorizon()];
        smab.updateTotals(prop, ticks, new HashMap<>());

        assertEquals(prop.getMax(), sens.getCurrentState(), 0);
    }

    @Test
    public void updateTotalsTestNoSensors() {
        PropertyWithData prop = new PropertyWithData();
        Actuator act = new Actuator("act", "subtype", "location", new ArrayList<>());
        prop.setActuator(act);
        prop.setDeltaWhenWork(11);
        final double sensInitState = 0.0;
        Sensor sens = new Sensor("sens name", "subtype", "location",
                sensInitState, new ArrayList<>());
        prop.setSensor(sens);
        prop.setMax(100);
        smab.iterationPowerConsumption = new double[dm_7_1_2.getHorizon()];
        List<Integer> ticks = Arrays.asList(1, 2, 3);

        //empty ticks lst, empty sensors map
        smab.updateTotals(prop, ticks, new HashMap<>());

        assertEquals(ticks.size() * prop.getDeltaWhenWork(), sens.getCurrentState(), 0);
        assertEquals(prop.getPowerConsumedInWork(), smab.iterationPowerConsumption[1], 0);
        assertEquals(prop.getPowerConsumedInWork(), smab.iterationPowerConsumption[2], 0);
        assertEquals(prop.getPowerConsumedInWork(), smab.iterationPowerConsumption[3], 0);
    }

    @Test
    public void flipCoinTestZeroProb() {
        boolean shouldBeFalse = smab.flipCoin(0);
        Assert.assertFalse(shouldBeFalse);
    }

    @Test
    public void flipCoinTestOneProb() {
        boolean shouldBeTrue = smab.flipCoin(1);
        Assert.assertTrue(shouldBeTrue);
    }

    @Test
    public void calcRangeOfWorkTestBefore() {
        List<Integer> expected = IntStream.range(0, 10).boxed().collect(Collectors.toList());

        PropertyWithData prop = new PropertyWithData();
        prop.setTargetTick(10);
        prop.setPrefix(Prefix.BEFORE);
        List<Integer> result = smab.calcRangeOfWork(prop);

        Assert.assertEquals(expected, result);
    }

    @Test
    public void calcRangeOfWorkTestAfter() {
        List<Integer> expected = IntStream.range(3, dm_7_1_2.getHorizon()).boxed().collect(Collectors.toList());

        PropertyWithData prop = new PropertyWithData();
        prop.setTargetTick(3);
        prop.setPrefix(Prefix.AFTER);
        List<Integer> result = smab.calcRangeOfWork(prop);

        Assert.assertEquals(expected, result);
    }

    @Test
    public void calcRangeOfWorkTestAt() {
        PropertyWithData prop = new PropertyWithData();
        prop.setTargetTick(3);
        prop.setPrefix(Prefix.AT);
        List<Integer> result = smab.calcRangeOfWork(prop);
        //bc the new change with AT
        Assert.assertTrue(result.contains(3));
        Assert.assertFalse(result.contains(4));
    }

    @Test
    public void calcBestPrice() {
        AlgoTestUtils.makeProp(props);
        smab.getHelper().setAllProperties(props);
        List<Integer> rangeForWork = smab.calcRangeOfWork(props.get(0));
        smab.buildScheduleBasic(false);
        List<Set<Integer>> subsets =  smab.getHelper().getSubsets(rangeForWork, 2);
        this.agent.setCurrIteration(new AgentIterationData(0, "h1", 100, smab.iterationPowerConsumption));
        List<Integer> res = smab.calcBestPrice(props.get(0), subsets);
        Assert.assertTrue(!res.isEmpty());
        Assert.assertTrue(res.size()==2);
        Assert.assertTrue(!res.contains(4));



    }

    @Test
    public void calcCsumTestAllZeros() {
        double[] mySched = getHorizonSizedArrWithSameVals(0);
        int neighboursSize = agent.getAgentData().getNeighbors().size();
        agent.setMyNeighborsShed(new ArrayList<>(neighboursSize));
        for (int i = 0; i < neighboursSize; i++) {
            //get an AgentIterationData with all zeros in sched.
            AgentIterationData iterData = new AgentIterationData(0, "bla", 0,
                    getHorizonSizedArrWithSameVals(0));
            agent.getMyNeighborsShed().add(iterData);
        }
        double cSum = smab.calcCsum(mySched);
        Assert.assertEquals(0, cSum, 0);
    }

    @Test
    public void calcCsumTestAllOnes() {
        double[] mySched = getHorizonSizedArrWithSameVals(1);
        int neighboursSize = agent.getAgentData().getNeighbors().size();
        agent.setMyNeighborsShed(new ArrayList<>(neighboursSize));
        for (int i = 0; i < neighboursSize; i++) {
            //get an AgentIterationData with all 1 in sched.
            AgentIterationData iterData = new AgentIterationData(0, "bla",
                    dm_7_1_2.getHorizon(), getHorizonSizedArrWithSameVals(1));
            agent.getMyNeighborsShed().add(iterData);
        }
        double cSum = smab.calcCsum(mySched);
        //each house pays the same
        double expected = Arrays.stream(dm_7_1_2.getPriceScheme()).sum() * (neighboursSize + 1);
        Assert.assertEquals(expected, cSum, 0.0000001);
    }

    @Test
    public void calcPriceTest() {
        double expected = 3.837;
        double[] allOnesSched = getHorizonSizedArrWithSameVals(1);
        double res = smab.calcPrice(allOnesSched);
        Assert.assertEquals(expected, res, 0);
    }

    @Test
    public void calcPriceTestAllZeros() {
        double expected = 0;
        final int val = 0;
        double[] allZerosSched = getHorizonSizedArrWithSameVals(val);
        double res = smab.calcPrice(allZerosSched);
        Assert.assertEquals(expected, res, 0);
    }

    private double[] getHorizonSizedArrWithSameVals(int val) {
        double[] arr = new double[dm_7_1_2.getHorizon()];
        for (int i = 0; i < dm_7_1_2.getHorizon(); i++) {
            arr[i] = val;
        }
        return arr;
    }

    @Test
    public void addBackgroundLoadToPowerConsumptionTest() {
        double[] allZerosSched = getHorizonSizedArrWithSameVals(0);
        smab.addBackgroundLoadToPowerConsumption(allZerosSched);
        Assert.assertArrayEquals(dm_7_1_2.getAgentsData().get(0).getBackgroundLoad(), allZerosSched, 0);
    }

}