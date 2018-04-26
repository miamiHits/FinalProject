package FinalProject.BL.Agents;

import FinalProject.BL.DataObjects.Problem;
import FinalProjectTests.BL.Agents.ReflectiveUtils;
import FinalProjectTests.DAL.DalTestUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class SHMGMTest {

    private Problem dm_7_1_2;
    private SHMGM shmgm;
    private SmartHomeAgent agent;

    @Before
    public void setUp() throws Exception {
        //needed for logging
        org.apache.log4j.BasicConfigurator.configure();

        //create a problem obj
        dm_7_1_2 = DalTestUtils.getProblemDm_7_1_2();
        agent = ReflectiveUtils.initSmartHomeAgentForTest(dm_7_1_2);
        shmgm = new SHMGM();
    }

    @After
    public void tearDown() throws Exception {
        dm_7_1_2 = null;
        shmgm = null;
        agent = null;
    }

    @Test
    public void countIterationCommunication() {
    }

    @Test
    public void generateScheduleForProp() {
    }

    @Test
    public void calcImproveOptionGrade() {
    }
}