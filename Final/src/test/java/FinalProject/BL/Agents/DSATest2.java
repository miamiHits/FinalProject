package FinalProject.BL.Agents;

import FinalProject.BL.Problems.*;
import FinalProject.DAL.DalTestUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DSATest2 {

    private Problem dm_7_1_2;
    private DSA dsa;
    private SmartHomeAgent agent;

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

    @Test
    public void someTest() {
        //TODO: Yarden check that this is ok:
        List<PropertyWithData> props = new ArrayList<>(2);

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
        chargeProp.setName("laundry_wash");
        chargeProp.setMin(0);
        chargeProp.setMax(60);
        chargeProp.setTargetValue(60);
        chargeProp.setPrefix(Prefix.BEFORE);
        chargeProp.setRt(RelationType.EQ);
        chargeProp.setTargetTick(6);
        chargeProp.setDeltaWhenWork(60);
        chargeProp.setPowerConsumedInWork(0.46);
        chargeProp.setDeltaWhenWorkOffline(0);
        Actuator GE_WSM2420D3WW_wash = new Actuator("GE_WSM2420D3WW_wash", "cloths_washer", "GE_WSM2420D3WW_wash",
                                        Arrays.asList(new Action("off", 0, Arrays.asList(new Effect("laundry_wash", 0))),
                                                      new Action("regular", 0.46,
                                                                 Arrays.asList(new Effect("laundry_wash", 60)))));
        Sensor GE_WSM2420D3WW_wash_sensor = new Sensor("GE_WSM2420D3WW_wash_sensor", "cloths_washer", "GE_WSM2420D3WW_wash", 60,
                                            Arrays.asList("laundry_wash"));
        chargeProp.setActuator(GE_WSM2420D3WW_wash);
        chargeProp.setSensor(GE_WSM2420D3WW_wash_sensor);
        props.add(laundryWashProp);

        try
        {
            ReflectiveUtils.invokeMethod(dsa, "buildScheduleFromScratch");

            Assert.assertEquals(dsa.helper.getAllProperties(), props);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e)
        {
            e.printStackTrace();
        }
    }
}
