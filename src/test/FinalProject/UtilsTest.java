package FinalProject;

import FinalProject.BL.Agents.ImprovementMsg;
import FinalProject.BL.Agents.SmartHomeAgent;
import jade.core.AID;
import jade.core.Agent;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Objects;

import static org.junit.Assert.*;

public class UtilsTest {

    @Before
    public void setUp() throws Exception {
        org.apache.log4j.BasicConfigurator.configure();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void parseAgentNameStrTest() {
        String fullName = "blabla@blabla:192.0.0.1";
        Assert.assertEquals("blabla", Utils.parseAgentName(fullName));
    }

    @Test
    public void getSizeOfObjInteger() {
        Integer integer = 3;
        Assert.assertEquals(81, Utils.getSizeOfObj(integer));
    }

    @Test
    public void getSizeOfObjImproMsg() {
        ImprovementMsg improMsg = new ImprovementMsg("bla",10, new double[12], new double[12]);
        Assert.assertEquals(382, Utils.getSizeOfObj(improMsg));
    }
}