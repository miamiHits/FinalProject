package FinalProject.BL.Agents;

import FinalProject.BL.IterationData.AgentIterationData;
import FinalProject.BL.Problems.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class DSATest {

    public AgentData ad = new AgentData("YC");;
    public SmartHomeAgent shg = new SmartHomeAgent();
    public List<Actuator> actuatorList = new ArrayList<>();
    public List<Sensor> sensorListList = new ArrayList<>();
    public List<Rule> ruleList = new ArrayList<>();
    public DSA dsa;
    public AlgorithmDataHelper dh;

    @Before
    public void setUp() throws Exception {

        dsa.setHelper(dh);
        createData();
        shg.setAgentData(ad);
        shg.setZEROIteration(true);
        dsa = new DSA(shg);
        dh = new AlgorithmDataHelper(dsa.agent);
    }
    @Before
    public void createData() {

        actuatorList = new ArrayList<Actuator>();
        BuildDevice1();
        BuildDevice2();

        createSensors();
        ad.setSensors(sensorListList);
        ad.setActuators(actuatorList);

        ad.setBackgroundLoad(new double[12]);
        ad.setPriceScheme(new double[12]);
    }
    @Before
    public void createRules() {
        Rule r = new Rule();
        r.setActive(true);
        r.setLocation("water_tank");
        r.setPrefixType(RelationType.GEQ);
        r.setRuleValue(57);
        r.setProperty("water_temp");
        r.setPrefix(Prefix.AFTER);
        r.setRelationValue(8);
      //  Rule r1 =new Rule( true, actuatorList.get(0), null, "water_temp", 57,  RelationType.GEQ, Prefix.AFTER, 8);
        Rule r2 =new Rule( false, null, "water_tank", "water_temp", 37,  RelationType.GEQ, null, 0);
        Rule r3 =new Rule( false, null, "water_tank", "water_temp", 78,  RelationType.LEQ, null, 0);
        Rule r4 =new Rule( true, actuatorList.get(1), null, "charge", 70,  RelationType.LT, Prefix.AFTER, 2);
        Rule r5 =new Rule( false, actuatorList.get(1), null, "charge", 78,  RelationType.GEQ, null, 0);
        Rule r6 =new Rule( false, actuatorList.get(1), null, "charge", 78,  RelationType.LEQ, null, 0);
        ruleList.add(r);
        ruleList.add(r2);
        ruleList.add(r3);
        ruleList.add(r4);
        ruleList.add(r5);
        ruleList.add(r6);
        ad.setRules(ruleList);

    }
    @Before
    public void createSensors() {
        List<String> sp = new ArrayList<>();
        sp.add("water_temp");
        Sensor sensor1 = new Sensor("water_heat_sensor", "", "", 50, sp);
        List<String> sp2 = new ArrayList<>();
        sp2.add("charge");
        Sensor sensor2 = new Sensor("Tesla_S_battery", "", "", 30, sp2);
        sensorListList.add(sensor1);
        sensorListList.add(sensor2);

    }
    @Before
    public void BuildDevice1() {
        List<Action> actionList = new ArrayList<>();
        List<Effect> effectsList1 = new ArrayList<>();
        List<Effect> effectsList2 = new ArrayList<>();

        effectsList1.add(new Effect("water_temp", -7.1));
        actionList.add(new Action("off", 0.0, effectsList1));
        effectsList2.add(new Effect("water_temp", 12.88));
        actionList.add(new Action("heat", 0.0, effectsList2));
        Actuator a = new Actuator("Rheem_XE40M12ST45U1", "", "", actionList);
        actuatorList.add(a);
    }
    @Before
    public void BuildDevice2() {
        List<Action> actionList = new ArrayList<>();
        List<Effect> effectsList1 = new ArrayList<>();
        List<Effect> effectsList2 = new ArrayList<>();

        effectsList1.add(new Effect("charge", 0.0));
        actionList.add(new Action("off", 0.0, effectsList1));
        effectsList2.add(new Effect("charge", 13.56));
        actionList.add(new Action("charge_48a", 0.0, effectsList2));
        Actuator a = new Actuator("Tesla_S", "", "", actionList);
        actuatorList.add(a);
    }

    @After
    public void tearDown() throws Exception {
        ad = new AgentData("YC");
        shg = new SmartHomeAgent();
        actuatorList = new ArrayList<>();
        sensorListList = new ArrayList<>();
        ruleList = new ArrayList<>();
    }

    @Test
    public void SimpleTest() {
        dsa.buildScheduleFromScratch();
        Assert.assertTrue(dsa.getHelper().allProperties.size()==2);
    }

    @Test
    public void doIteration() {
        AgentIterationData ag = new AgentIterationData(0, "YC", 0 , new double[12]);
        Assert.assertTrue(dsa.agentIterationData.equals(ag));
    }



    @Test
    public void sendIterationToCollector() {
    }

    @Test
    public void action() {

    }

    @Test
    public void done() {
    }
}