package FinalProject.BL.Agents;

import FinalProject.BL.DataObjects.Problem;
import FinalProjectTests.DAL.DalTestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class SmartHomeAgentBehaviourTest {

    private SmartHomeAgent agent;
    private Problem dm_7_1_2;

    @Before
    public void setUp() throws Exception {
        //needed for logging
        org.apache.log4j.BasicConfigurator.configure();

        //create a problem obj
        dm_7_1_2 = DalTestUtils.getProblemDm_7_1_2();
        agent = new SmartHomeAgent();
    }

    @After
    public void tearDown() throws Exception {
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

    }

    @Test
    public void updateTotals() {
    }

    @Test
    public void flipCoin() {
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
    public void calcPrice() {
    }

    @Test
    public void addBackgroundLoadToPowerConsumption() {
    }
}