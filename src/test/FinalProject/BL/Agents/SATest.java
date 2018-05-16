package FinalProject.BL.Agents;

import FinalProject.BL.DataObjects.Problem;
import FinalProject.BL.IterationData.AgentIterationData;
import FinalProject.DAL.DalTestUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

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
    public void generateScheduleForProp() {
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
    public void cloneBehaviour() {
    }

    @Test
    public void calcImproveOptionGrade() {
    }

    @Test
    public void equals() {
    }
}