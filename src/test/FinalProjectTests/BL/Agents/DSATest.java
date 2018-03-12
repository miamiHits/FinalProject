package FinalProjectTests.BL.Agents;

import FinalProject.BL.Agents.*;
import FinalProject.BL.DataObjects.*;
import FinalProjectTests.DAL.DalTestUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import FinalProject.BL.*;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DSATest {

    private Problem dm_7_1_2;
    private DSA dsa;
    private SmartHomeAgent agent;
    private List<PropertyWithData> props = new ArrayList<>(2);

    @Before
    public void setup() {
        //needed for logging
        org.apache.log4j.BasicConfigurator.configure();

        //create a problem obj
        dm_7_1_2 = DalTestUtils.getProblemDm_7_1_2();
        agent = new SmartHomeAgent();

        AgentData agentData = dm_7_1_2.getAgentsData().get(0);
        String problemId = dm_7_1_2.getId();
        try
        {
            ReflectiveUtils.setFiledValue(agentData, "priceScheme", dm_7_1_2.getPriceScheme());

            //agent.setup() will not be called so we'll do it manually
            ReflectiveUtils.setFiledValue(agent, "agentData", agentData);
            ReflectiveUtils.setFiledValue(agent, "problemId", problemId);
            ReflectiveUtils.setFiledValue(agent, "algoId", "DSA");
            ReflectiveUtils.setFiledValue(agent, "isZEROIteration", true);

        } catch (Exception e)
        {
            e.printStackTrace();
        }
        dsa = new DSA(agent);
    }

    @After
    public void tearDown() {
        dm_7_1_2 = null;
        dsa = null;
        agent = null;
    }

    public void prepareGround()
    {
        PropertyWithData chargeProp = new PropertyWithData();
        chargeProp.setName("charge");
        chargeProp.setMin(0);
        chargeProp.setMax(100);
        chargeProp.setTargetValue(59);
        chargeProp.setPrefix(Prefix.BEFORE);
        chargeProp.setRt(RelationType.LEQ);
        chargeProp.setTargetTick(3);
        chargeProp.setDeltaWhenWork(13.56);
        chargeProp.setPowerConsumedInWork(11.52);
        chargeProp.setDeltaWhenWorkOffline(0);
        chargeProp.setCachedSensor(30);
        chargeProp.setLoaction(false);
        Actuator tesla_s = new Actuator("Tesla_S", "electric_vehicle", "room",
                Arrays.asList(new Action("off", 0, Arrays.asList(new Effect("charge", 0))),
                        new Action("charge_48a", 11.52,
                                Arrays.asList(new Effect("charge", 13.56)))));
        Sensor Tesla_S_battery = new Sensor("Tesla_S_battery", "battery", "Tesla_S", 70.68,
                Arrays.asList("charge"));
        chargeProp.setActuator(tesla_s);
        chargeProp.setSensor(Tesla_S_battery);
        props.add(chargeProp);

        PropertyWithData laundryWashProp = new PropertyWithData();
        laundryWashProp.setName("laundry_wash");
        laundryWashProp.setMin(0);
        laundryWashProp.setMax(60);
        laundryWashProp.setTargetValue(60);
        laundryWashProp.setPrefix(Prefix.BEFORE);
        laundryWashProp.setRt(RelationType.EQ);
        laundryWashProp.setTargetTick(6);
        laundryWashProp.setDeltaWhenWork(60);
        laundryWashProp.setPowerConsumedInWork(0.46);
        laundryWashProp.setDeltaWhenWorkOffline(0);
        laundryWashProp.setLoaction(false);
        Actuator GE_WSM2420D3WW_wash = new Actuator("GE_WSM2420D3WW_wash", "cloths_washer", "GE_WSM2420D3WW_wash",
                Arrays.asList(new Action("off", 0, Arrays.asList(new Effect("laundry_wash", 0))),
                        new Action("regular", 0.46,
                                Arrays.asList(new Effect("laundry_wash", 60)))));
        Sensor GE_WSM2420D3WW_wash_sensor = new Sensor("GE_WSM2420D3WW_wash_sensor", "cloths_washer", "GE_WSM2420D3WW_wash", 60,
                Arrays.asList("laundry_wash"));
        laundryWashProp.setActuator(GE_WSM2420D3WW_wash);
        laundryWashProp.setSensor(GE_WSM2420D3WW_wash_sensor);
        props.add(laundryWashProp);
    }

    public void prepareGround2()
    {
        PropertyWithData tempHot = new PropertyWithData();
        tempHot.setName("temp_hot");
        tempHot.setMin(37);
        tempHot.setMax(73);
        tempHot.setTargetValue(59);
        tempHot.setPrefix(Prefix.AT);
        tempHot.setRt(RelationType.LEQ);
        tempHot.setTargetTick(3);
        tempHot.setDeltaWhenWork(13.56);
        tempHot.setPowerConsumedInWork(11.52);
        tempHot.setDeltaWhenWorkOffline(0);
        tempHot.setCachedSensor(30);
        tempHot.setLoaction(false);
        Actuator Roomba = new Actuator("Roomba", "", "room",
                Arrays.asList(new Action("off", 0, Arrays.asList(new Effect("temp_hot", 0))),
                        new Action("cleaness", 11.52,
                                Arrays.asList(new Effect("temp_hot", 13.56)))));
        Sensor Roomba_sens = new Sensor("Roomba_sens", "", "Roomba", 40,
                Arrays.asList("temp_hot"));
        tempHot.setActuator(Roomba);
        tempHot.setSensor(Roomba_sens);
        props.add(tempHot);
    }
    @Test
    public void buildScheduleFromScratchPropertiesTest() {
        this.props.clear();
        this.props = new ArrayList<>();
        prepareGround();
        this.dsa.doIteration();
        try
        {
            ReflectiveUtils.invokeMethod(dsa, "buildScheduleFromScratch");
            Assert.assertTrue(props.get(0).getSensor().getCurrentState()> props.get(0).getMin());
            Assert.assertTrue(props.get(0).getSensor().getCurrentState()<= props.get(0).getMax());
            Assert.assertTrue(props.get(1).getSensor().getCurrentState()> props.get(1).getMin());
            Assert.assertTrue(props.get(1).getSensor().getCurrentState()<= props.get(1).getMax());

        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e)
        {
            e.printStackTrace();
        }
    }

    @Test
    public void buildScheduleFromScratchPropertiesTestSpecailCase() {
        this.props.clear();
        this.props = new ArrayList<>();
        prepareGround2();
        this.dsa.doIteration();
        for(Map.Entry<Actuator, List<Integer>> entry: dsa.getHelper().getDeviceToTicks().entrySet())
        {
            if (entry.getKey().getName().equals("Roomba"))
            {
                Assert.assertTrue(entry.getValue().contains(3));
                Assert.assertTrue(entry.getValue().contains(2) || entry.getValue().contains(0) || entry.getValue().contains(1));
            }
        }
    }

    @Test
    public void PowerConsTest(){
        this.props.clear();
        this.props = new ArrayList<>();
        prepareGround();
        dsa.doIteration();
        double counter = 0;
        for (int i=0; i<this.dsa.getPowerConsumption().length; i++)
        {
            counter = Double.sum(counter, this.dsa.getPowerConsumption()[i]);
        }
        Assert.assertTrue(counter > 0);
    }

    @Test
    public void DevicesGetTicks(){
        this.props.clear();
        this.props = new ArrayList<>();
        prepareGround();
        this.dsa.doIteration();
        Assert.assertTrue(this.dsa.getHelper().getDeviceToTicks().size() == 2);

    }

    @Test
    public void GranularityTest() {
        this.props.clear();
        this.props = new ArrayList<>();
        prepareGround();
        this.agent.getAgentData().setGranularity(120);
        try
        {
            ReflectiveUtils.invokeMethod(dsa, "buildScheduleFromScratch");


        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e)
        {
            e.printStackTrace();
        }
    }

    @Test
    public void dsaIterBuildSched(){
        this.props.clear();
        this.props = new ArrayList<>();
        this.dsa.agent.setcSum(1000);
        prepareGround();
        try
        {
            ReflectiveUtils.invokeMethod(dsa, "buildScheduleFromScratch");
            this.dsa.agent.setZEROIteration(false);
            ReflectiveUtils.invokeMethod(dsa, "tryBuildSchedule");
            Assert.assertTrue(props.get(0).getSensor().getCurrentState()> props.get(0).getMin());
            Assert.assertTrue(props.get(0).getSensor().getCurrentState()<= props.get(0).getMax());
            Assert.assertTrue(props.get(1).getSensor().getCurrentState()> props.get(1).getMin());
            Assert.assertTrue(props.get(1).getSensor().getCurrentState()<= props.get(1).getMax());

        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e)
        {
            e.printStackTrace();
        }

    }

    
}
