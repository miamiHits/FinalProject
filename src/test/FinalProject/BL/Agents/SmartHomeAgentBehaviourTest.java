package FinalProject.BL.Agents;

import FinalProject.BL.DataObjects.Prefix;
import FinalProject.BL.DataObjects.Problem;
import FinalProject.BL.DataObjects.Rule;
import FinalProject.BL.IterationData.AgentIterationData;
import FinalProjectTests.BL.Agents.ReflectiveUtils;
import FinalProjectTests.DAL.DalTestUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import sun.misc.GC;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

public class SmartHomeAgentBehaviourTest {

    private SmartHomeAgent agent;
    private Problem dm_7_1_2;
    private SmartHomeAgentBehaviour smab;

    @Before
    public void setUp() throws Exception {
        //needed for logging
        org.apache.log4j.BasicConfigurator.configure();

        //create a problem obj
        dm_7_1_2 = DalTestUtils.getProblemDm_7_1_2();
        agent = ReflectiveUtils.initSmartHomeAgentForTest(dm_7_1_2);
        smab = new DSA(agent);
        smab.initializeBehaviourWithAgent(agent);

    }

    @After
    public void tearDown() throws Exception {
        dm_7_1_2 = null;
        smab = null;
        agent = null;
    }

    @Test
    public void buildScheduleFromScratch() {
    }

    @Test
    public void calcHowManyTicksNeedToCharge() {
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
    public void updateTotals() {
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
        List<Integer> expected = Collections.singletonList(3);

        PropertyWithData prop = new PropertyWithData();
        prop.setTargetTick(3);
        prop.setPrefix(Prefix.AT);
        List<Integer> result = smab.calcRangeOfWork(prop);

        Assert.assertEquals(expected, result);
    }

    @Test
    public void calcBestPrice() {
        //TODO: maybe should be in sub classes
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