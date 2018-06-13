package FinalProject.BL.Agents;

import FinalProject.BL.DataCollection.DataCollectionCommunicator;
import FinalProject.BL.DataCollection.PowerConsumptionUtils;
import FinalProject.BL.DataObjects.*;
import FinalProject.BL.IterationData.AgentIterationData;
import FinalProject.BL.IterationData.IterationCollectedData;
import FinalProject.Config;
import FinalProject.DAL.DalTestUtils;
import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.junit.*;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DBATest {

    private Problem dm_7_1_2;
    private DBA dba;
    private SmartHomeAgent agent;
    private List<PropertyWithData> props;
    private double oldPrice2;
    private double newPrice2;
    private int [] ticksBag = new int [12];
    private int [] newTicksbags = new int[12];
    @Before
    public void setup() {
        Config.loadTestConfig();
        //needed for logging
        org.apache.log4j.BasicConfigurator.configure();

        //create a problem obj
        dm_7_1_2 = DalTestUtils.getProblemDm_7_1_2();
        agent = ReflectiveUtils.initSmartHomeAgentForTest(dm_7_1_2);
        dba = new TestDBA(agent, 6);
        props = new ArrayList<>(2);
        dba.initializeBehaviourWithAgent(agent);
        this.oldPrice2 =0;
        this.newPrice2 =0;
        cleanBagsBackToOne();
    }

    @After
    public void tearDown() {
        dm_7_1_2 = null;
        dba = null;
        agent = null;
        props = null;
        cleanBagsBackToOne();
    }

    private void cleanBagsBackToOne()
    {
        for(int i=0; i<ticksBag.length; i++)
        {
            ticksBag[i] = 1;
            newTicksbags[i] = 1;
        }
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
        this.dba.getHelper().setAllProperties(this.props);
        this.dba.buildScheduleFromScratch();
        try
        {
            ReflectiveUtils.invokeMethod(dba, "buildScheduleFromScratch");
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

    public void buildScheduleFromScratchPropertiesTestSpecialCase() {
        this.props.clear();
        this.props = new ArrayList<>();
        prepareGround();
        AlgoTestUtils.makeProp(props);
        this.dba.getHelper().setAllProperties(this.props);
        this.dba.buildScheduleFromScratch();
        for(Map.Entry<Actuator, Map<Action, List<Integer>>> entry: dba.getHelper().getDeviceToTicks().entrySet()) {
            for (Map.Entry<Action, List<Integer>> res: entry.getValue().entrySet()) {
                if (entry.getKey().getName().equals("GE_WSM2420D3WW_wash")){ // need to work only 1 Tick.
                    Assert.assertTrue(res.getValue().size()==1);
                }
                else if (entry.getKey().getName().equals("Tesla_S")) // need to work 3 Ticks.
                {
                    Assert.assertTrue(res.getValue().size()==3);
                }
                else if (entry.getKey().getName().equals("Roomba")) {
                    Assert.assertTrue(res.getValue().contains(0) || res.getValue().contains(1) || res.getValue().contains(2));
                }
            }
        }
    }

    @Test
    public void PowerConsTest(){
        this.props.clear();
        this.props = new ArrayList<>();
        prepareGround();
        dba.buildScheduleFromScratch();
        double counter = 0;
        for (int i=0; i<this.dba.getPowerConsumption().length; i++)
        {
            counter = Double.sum(counter, this.dba.getPowerConsumption()[i]);
        }
        Assert.assertTrue(counter > 0);
    }

    @Test
    public void DevicesGetTicks(){
        this.props.clear();
        this.props = new ArrayList<>();
        AlgoTestUtils.makeProp(props);
        this.dba.getHelper().setAllProperties(this.props);
        this.dba.buildScheduleFromScratch();
        Assert.assertTrue(this.dba.getHelper().getDeviceToTicks().size() == 3);

    }

    @Test
    public void GranularityTest() {
        this.props.clear();
        this.props = new ArrayList<>();
        prepareGround();
        this.agent.getAgentData().setGranularity(120);
        try
        {
            ReflectiveUtils.invokeMethod(dba, "buildScheduleFromScratch");


        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e)
        {
            e.printStackTrace();
        }
    }

    @Test
    public void countIterationCommunicationTest() {
        try {
            this.dba = new DBA(agent);
            Set<String> neighborhood = agent.getAgentData().getNeighbors().stream()
                    .map(AgentData::getName)
                    .collect(Collectors.toSet());
            AgentIterationData aid = new AgentIterationData(0, "name", 10.5, new double[]{});
            IterationCollectedData icd = new IterationCollectedData(0, "name", 10.5, new double[]{},
                    "testProblem", "testAlgo", neighborhood, 10, 1, 1);
            ReflectiveUtils.setFieldValue((DBA)dba, "agentIterationCollected", icd);
            ReflectiveUtils.setFieldValue((DBA)dba, "agentIterationData", aid);
            ReflectiveUtils.invokeMethod((DBA)dba, "initHelper");
            ReflectiveUtils.invokeMethod((DBA)dba, "countIterationCommunication");
            long size = agent.getIterationMessageSize();
            int count = agent.getIterationMessageCount();
            Assert.assertEquals(1190,size);
            Assert.assertEquals(15, count);
        } catch (Exception e) {
            System.out.println(e);
            Assert.fail();
        }
    }

    @Test
    public void checkMainLogicDBA() {
        dba.buildScheduleBasic(false);
        double[] prevIterPowerConsumption = dba.helper.cloneArray(dba.iterationPowerConsumption); //equals to agent.getCurrIteration().powerConsumptionPerTick
        this.oldPrice2 = dba.calcCsum(prevIterPowerConsumption);
        dba.mainLogicDBA();
        Assert.assertTrue(oldPrice2 <= newPrice2);
    }

    @Test
    public void bagsArr() {
        dba.buildScheduleBasic(false);
        double[] prevIterPowerConsumption = dba.helper.cloneArray(dba.iterationPowerConsumption); //equals to agent.getCurrIteration().powerConsumptionPerTick
        this.oldPrice2 = dba.calcCsum(prevIterPowerConsumption);
        dba.mainLogicDBA();
        for (int i =0 ; i<this.ticksBag.length; i++)
        {
            Assert.assertTrue(this.ticksBag[i] <= this.newTicksbags[i]);
        }
    }


    private class TestDBA extends DBA
    {
        //used for affecting the behaviour of DBA, creating scenarios with expected behaviour without involving jade

        public int currentNeighbourCount;
        public int expectedImprovementFromAllNeighbours;


        public TestDBA(SmartHomeAgent agent, int currentNeighbourCount)
        {
            super(agent);
            this.currentNeighbourCount = currentNeighbourCount;


        }

        // returns
        @Override
        protected List<ImprovementMsg> receiveImprovementMsgs() {
            List<ImprovementMsg> fakeMessages = new ArrayList<>();
            for (int i = 0; i < currentNeighbourCount; i++)
            {
                ImprovementMsg message = new ImprovementMsg("bla",this.expectedImprovementFromAllNeighbours, new double[12], new double[12]);
                fakeMessages.add(message);
            }

            return fakeMessages;
        }

        // returns
        @Override
        protected void mainLogicDBA()
        {
            boolean[] bitMapBags = new boolean[12];
            Map<Actuator, Map<Action, List<Integer>>> helperTicksToDevice = helper.getDeviceToTicks();

            // add bags
            for (Map.Entry<Actuator, Map<Action, List<Integer>>> entry: helperTicksToDevice.entrySet())
            {
                for (Map.Entry<Action, List<Integer>> innerEntry : entry.getValue().entrySet())
                {
                    for (int tick: innerEntry.getValue())
                    {
                        bitMapBags[tick] = true;
                    }
                }
            }

            IntStream.range(0, ticksBag.length).filter(i -> bitMapBags[i]).forEach(i -> ticksBag[i]++);
            IntStream.range(0, ticksBag.length).filter(i -> bitMapBags[i]).forEach(i -> newTicksbags[i]++);

            dba.setInImprovementRound(true);

            //reset fields
            // double prevTotalCost = helper.calcTotalPowerConsumption(oldPrice, iterationPowerConsumption); //also sets helper's epeak
            agent.setPriceSum(oldPrice2);
            helper.resetProperties();
            //calc new sched according to new bags
            buildScheduleBasic(false);

            //build new sched according to new bags
            newPrice2 = calcCsum(iterationPowerConsumption); //iterationPowerConsumption changed by buildScheduleBasic
            dba.setInImprovementRound(false);

        }
    }
}
