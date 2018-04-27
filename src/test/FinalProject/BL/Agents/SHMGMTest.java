package FinalProject.BL.Agents;

import FinalProject.BL.DataCollection.PowerConsumptionUtils;
import FinalProject.BL.DataObjects.Problem;
import FinalProject.BL.IterationData.AgentIterationData;
import FinalProject.DAL.DalTestUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
        shmgm.initializeBehaviourWithAgent(agent);
    }

    @After
    public void tearDown() throws Exception {
        dm_7_1_2 = null;
        shmgm = null;
        agent = null;
    }

    @Test
    public void countIterationCommunicationTestTotalSize() throws NoSuchFieldException, IllegalAccessException {
        AgentIterationData iterationData = new AgentIterationData(10,
                "Test", 1, new double[12]);
        ReflectiveUtils.setFieldValue(shmgm, "agentIterationData", iterationData);
        shmgm.countIterationCommunication();
        Assert.assertEquals(1734, agent.getIterationMessageSize());
    }

    @Test
    public void countIterationCommunicationTestMsgCount() throws NoSuchFieldException, IllegalAccessException {
        AgentIterationData iterationData = new AgentIterationData(10,
                "Test", 1, new double[12]);
        ReflectiveUtils.setFieldValue(shmgm, "agentIterationData", iterationData);
        shmgm.countIterationCommunication();
        Assert.assertEquals(7, agent.getIterationMessageCount());
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
        double expected = shmgm.calcCsum(powerCons);
        allScheds.add(powerCons);
        expected += PowerConsumptionUtils.calculateEPeak(allScheds);
        double res = shmgm.calcImproveOptionGrade(powerCons, allScheds);
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
        double res = shmgm.calcImproveOptionGrade(powerCons, allScheds);
        Assert.assertEquals(expected, res, 0.0);
    }

}