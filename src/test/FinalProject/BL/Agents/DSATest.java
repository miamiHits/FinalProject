package FinalProject.BL.Agents;

import FinalProject.BL.DataObjects.*;
import FinalProject.BL.IterationData.AgentIterationData;
import FinalProject.BL.IterationData.IterationCollectedData;
import FinalProject.Config;
import FinalProject.DAL.DalTestUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class DSATest {

    private Problem dm_7_1_2;
    private DSA dsa;
    private SmartHomeAgent agent;
    private List<PropertyWithData> props;

    @Before
    public void setup() {
        Config.loadTestConfig();
        //needed for logging
        org.apache.log4j.BasicConfigurator.configure();

        //create a problem obj
        dm_7_1_2 = DalTestUtils.getProblemDm_7_1_2();
        agent = ReflectiveUtils.initSmartHomeAgentForTest(dm_7_1_2);
        dsa = new DSA(agent);
        props = new ArrayList<>(2);
    }

    @After
    public void tearDown() {
        dm_7_1_2 = null;
        dsa = null;
        agent = null;
        props = null;
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

    @Test
    public void buildScheduleFromScratchPropertiesTest() {
        this.props.clear();
        this.props = new ArrayList<>();
        prepareGround();
        AlgoTestUtils.makeProp(props);
        this.dsa.getHelper().setAllProperties(this.props);
        this.dsa.buildScheduleFromScratch();
        try
        {
            ReflectiveUtils.invokeMethod(dsa, "buildScheduleFromScratch");
            Assert.assertTrue(props.get(0).getSensor().getCurrentState()> props.get(0).getMin());
            Assert.assertTrue(props.get(0).getSensor().getCurrentState()<= props.get(0).getMax());
            Assert.assertTrue(props.get(1).getSensor().getCurrentState()> props.get(1).getMin());
            Assert.assertTrue(props.get(1).getSensor().getCurrentState()<= props.get(1).getMax());
            Assert.assertTrue(props.get(2).getSensor().getCurrentState()> props.get(2).getMin());
            Assert.assertTrue(props.get(2).getSensor().getCurrentState()<= props.get(2).getMax());

        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e)
        {
            e.printStackTrace();
        }
    }

    @Test
    public void buildScheduleFromScratchPropertiesTestSpecialCase() {
        prepareGround();
        AlgoTestUtils.makeProp(props);
        this.dsa.getHelper().setAllProperties(this.props);
        this.dsa.buildScheduleFromScratch();
        for(Entry<Actuator, Map<Action, List<Integer>>> entry: dsa.getHelper().getDeviceToTicks().entrySet()) {
            for (Entry<Action, List<Integer>> res: entry.getValue().entrySet()) {
                switch (entry.getKey().getName()) {
                    case "GE_WSM2420D3WW_wash":
                        Assert.assertTrue(res.getValue().size() == 1 || res.getValue().size() == 0);
                        break;
                    case "Tesla_S":
                        Assert.assertTrue(res.getValue().size() == 3 || res.getValue().size() == 0);
                        break;
                    case "Roomba":
                        Assert.assertTrue(res.getValue().contains(0) || res.getValue().contains(1) || res.getValue().contains(2));
                        break;
                }
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
        AlgoTestUtils.makeProp(props);
        this.dsa.getHelper().setAllProperties(this.props);
        this.dsa.buildScheduleFromScratch();
        Assert.assertTrue(this.dsa.getHelper().getDeviceToTicks().size() == 3);

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
        this.dsa.agent.setPriceSum(1000);
        prepareGround();
        try
        {
            ReflectiveUtils.invokeMethod(dsa, "buildScheduleFromScratch");
            this.dsa.agent.setZEROIteration(false);
            ReflectiveUtils.invokeMethod(dsa, "resetAndBuildSchedule");
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
    public void countIterationCommunicationTest() {
        try {
            Set<String> neighborhood = agent.getAgentData().getNeighbors().stream()
                    .map(AgentData::getName)
                    .collect(Collectors.toSet());
            AgentIterationData aid = new AgentIterationData(0, "name", 10.5, new double[]{});
            IterationCollectedData icd = new IterationCollectedData(0, "name", 10.5, new double[]{},
                    "testProblem", "testAlgo", neighborhood, 10, 1, 1);
            ReflectiveUtils.setFieldValue(dsa, "agentIterationCollected", icd);
            ReflectiveUtils.setFieldValue(dsa, "agentIterationData", aid);
            ReflectiveUtils.invokeMethod(dsa, "initHelper");
            ReflectiveUtils.invokeMethod(dsa, "countIterationCommunication");
            long size = agent.getIterationMessageSize();
            int count = agent.getIterationMessageCount();
            Assert.assertEquals(1190,size);
            Assert.assertEquals(15, count);
        } catch (Exception e) {
            System.out.println(e);
            Assert.fail();
        }
    }
}
