package FinalProject.BL.Agents;

import FinalProject.BL.DataObjects.Problem;
import FinalProjectTests.BL.Agents.ReflectiveUtils;
import FinalProjectTests.DAL.DalTestUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import sun.misc.GC;

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
    public void initHelper() {
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
    public void calcRangeOfWork() {
    }

    @Test
    public void calcBestPrice() {
    }

    @Test
    public void calcCsum() {
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