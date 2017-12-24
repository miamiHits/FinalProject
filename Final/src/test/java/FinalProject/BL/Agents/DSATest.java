package FinalProject.BL.Agents;

import FinalProject.BL.Problems.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class DSATest {

    @Before
    public void setUp() throws Exception {
        AgentData ad = new AgentData("bo_13_1_6");
        createData(ad);
        SmartHomeAgent shg = new SmartHomeAgent();
        shg.setAgentData(ad);
        shg.setZEROIteration(true);
        DSA dsa = new DSA("YC", shg);
    }
    @Before
    private void createData(AgentData ad) {

        List<Actuator> actuatorList = new ArrayList<>();
        actuatorList.add(BuildDevice1());
        actuatorList.add(BuildDevice2());
        ad.setActuators(actuatorList);

        List<Sensor> sensorListList = createSensors();
        ad.setSensors(sensorListList);

        List<Rule> ruleList = createRules(actuatorList, sensorListList);
        ad.setRules(ruleList);

        ad.setBackgroundLoad(new double[12]);
        ad.setPriceScheme(new double[12]);
        ad.setName("YC");
    }
    @Before
    private List<Rule> createRules(List<Actuator> actuatorList, List<Sensor> sensorListList) {
        List<Rule> ruleList = new ArrayList<>();
        Rule r1 =new Rule( true, actuatorList.get(0), null, "water_temp", 57,  RelationType.GEQ, null, 0);
        Rule r2 =new Rule( false, actuatorList.get(0), null, "water_temp", 37,  RelationType.GEQ, null, 0);
        Rule r3 =new Rule( false, actuatorList.get(0), null, "water_temp", 78,  RelationType.LEQ, Prefix.AFTER, 8);
        Rule r4 =new Rule( true, actuatorList.get(1), null, "charge", 70,  RelationType.LT, Prefix.AFTER, 2);
        Rule r5 =new Rule( false, actuatorList.get(1), null, "charge", 78,  RelationType.GEQ, null, 0);
        Rule r6 =new Rule( false, actuatorList.get(1), null, "charge", 78,  RelationType.LEQ, null, 0);
        ruleList.add(r1);
        ruleList.add(r2);
        ruleList.add(r3);
        ruleList.add(r4);
        ruleList.add(r5);
        ruleList.add(r6);
        return ruleList;
    }
    @Before
    private List<Sensor> createSensors() {
        List<Sensor> sensorListList = new ArrayList<>();
        List<String> sp = new ArrayList<>();
        sp.add("water_temp");
        Sensor sensor1 = new Sensor("water_heat_sensor", null, null, 0, sp);
        List<String> sp2 = new ArrayList<>();
        sp2.add("charge");
        Sensor sensor2 = new Sensor("Tesla_S_battery", null, null, 30, sp2);
        sensorListList.add(sensor1);
        sensorListList.add(sensor2);

        return sensorListList;
    }
    @Before
    private Actuator BuildDevice1() {
        List<Action> actionList = new ArrayList<>();
        List<Effect> effectsList1 = new ArrayList<>();
        List<Effect> effectsList2 = new ArrayList<>();

        effectsList1.add(new Effect("water_temp", -7.1));
        actionList.add(new Action("off", 0.0, effectsList1));
        effectsList2.add(new Effect("water_temp", 12.88));
        actionList.add(new Action("heat", 0.0, effectsList2));
       return new Actuator("Rheem_XE40M12ST45U1", null, null, actionList);
    }
    @Before
    private Actuator BuildDevice2() {
        List<Action> actionList = new ArrayList<>();
        List<Effect> effectsList1 = new ArrayList<>();
        List<Effect> effectsList2 = new ArrayList<>();

        effectsList1.add(new Effect("charge", 0.0));
        actionList.add(new Action("off", 0.0, effectsList1));
        effectsList2.add(new Effect("charge", 13.56));
        actionList.add(new Action("charge_48a", 0.0, effectsList2));
        return new Actuator("Tesla_S", null, null, actionList);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void doIteration() {

    }

    @Test
    public void sendIterationToCollector() {
    }

    @Test
    public void action() {
        Assert.assertTrue("yarden".endsWith("n"));
    }

    @Test
    public void done() {
    }
}