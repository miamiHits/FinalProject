package FinalProjectTests.BL.Agents;

import FinalProject.BL.DataObjects.*;
import FinalProject.BL.Agents.*;
import FinalProject.BL.DataCollection.*;
import org.junit.*;

import java.util.ArrayList;
import java.util.Arrays;

public class PropertyWithDataTest
{

    public static final int HORIZON_SIZE = 12;

    private PropertyWithData chargeProp;

    @Before
    public void setup()
    {
        this.chargeProp = new PropertyWithData();
        chargeProp.setName("charge");
        chargeProp.setMin(0);
        chargeProp.setMax(100);
        chargeProp.setTargetValue(60);
        chargeProp.setPrefix(Prefix.BEFORE);
        chargeProp.setRt(RelationType.GEQ);
        chargeProp.setPowerConsumedInWork(10);
        chargeProp.setCachedSensor(0);
        chargeProp.setLoaction(false);
        Sensor Tesla_S_battery = new Sensor("Tesla_S_battery", "battery", "Tesla_S", 0,
                Arrays.asList("charge"));
        chargeProp.setSensor(Tesla_S_battery);
        chargeProp.setTargetTick(10);
        chargeProp.setDeltaWhenWork(20);
        chargeProp.setDeltaWhenWorkOffline(0);
        Actuator tesla_s = new Actuator("Tesla_S", "electric_vehicle", "room",
                Arrays.asList(new Action("off", 0,
                                Arrays.asList(new Effect("charge", 0))),
                        new Action("charge_48a", 10,
                                Arrays.asList(new Effect("charge", 10)))));
        chargeProp.setActuator(tesla_s);
        final ArrayList<Integer> activeTicks = new ArrayList<>();
        activeTicks.add(0);
        activeTicks.add(1);
        activeTicks.add(2);
        chargeProp.activeTicks = activeTicks;



    }


    //////////////////////////////////////////////////////
    /// calcAndUpdateCurrState
    //////////////////////////////////////////////////////

    @After
    public void tearDown()
    {

    }

    @Test
    /**
     * tested method calcAndUpdateCurrState
     * checks no additional application are added when the offline delta is not negative
     * initial state = 0
     * states: off delta = 0, on delta = +20
     * should have 60 before tick 3, ticks 0-2 were chosen to be on, isFromStart argument is false
     */
    public void calcAndUpdateCurrState_noRedundantApplicationAdded()
    {
        final double TARGET_TICK = 3;
        final boolean IS_FROM_START = true;
        final double ON_DELTA = 20;
        final double OFF_DELTA = 0;
        final ArrayList<Integer> activeTicks = new ArrayList<>();
        activeTicks.add(0);
        activeTicks.add(1);
        activeTicks.add(2);


        chargeProp.setTargetTick(TARGET_TICK);
        chargeProp.setDeltaWhenWork(ON_DELTA);
        chargeProp.setDeltaWhenWorkOffline(OFF_DELTA);
        Actuator tesla_s = new Actuator("Tesla_S", "electric_vehicle", "room",
                Arrays.asList(new Action("off", 0,
                                Arrays.asList(new Effect("charge", OFF_DELTA))),
                        new Action("charge_48a", 10,
                                Arrays.asList(new Effect("charge", ON_DELTA)))));
        chargeProp.setActuator(tesla_s);

        double[] consumption = new double[HORIZON_SIZE];
        chargeProp.activeTicks = activeTicks;
        chargeProp.calcAndUpdateCurrState(chargeProp.getMin(), HORIZON_SIZE, consumption, IS_FROM_START);
        assertPropertyState(chargeProp, 0 , HORIZON_SIZE, ON_DELTA, OFF_DELTA);
    }

    @Test
    /**
     * tested method calcAndUpdateCurrState
     * checks if the target, min and max values for the property are kept
     * initial state = 0
     * states: off delta = -10, on delta = +20
     * should have 60 before tick 3, ticks 0-2 were chosen to be on, isFromStart argument is false
     */
    public void calcAndUpdateCurrState_targetPassiveValuesAreKept()
    {
        final double TARGET_TICK = 3;
        final boolean IS_FROM_START = true;
        final double OFF_DELTA = -10;
        final double ON_DELTA = 20;
        final ArrayList<Integer> activeTicks = new ArrayList<>();
        activeTicks.add(0);
        activeTicks.add(1);
        activeTicks.add(2);


        chargeProp.setTargetTick(TARGET_TICK);
        chargeProp.setDeltaWhenWork(ON_DELTA);
        chargeProp.setDeltaWhenWorkOffline(OFF_DELTA);
        Actuator tesla_s = new Actuator("Tesla_S", "electric_vehicle", "room",
                Arrays.asList(new Action("off", 0,
                                Arrays.asList(new Effect("charge", OFF_DELTA))),
                        new Action("charge_48a", 10,
                                Arrays.asList(new Effect("charge", ON_DELTA)))));
        chargeProp.setActuator(tesla_s);
        double[] consumption = new double[HORIZON_SIZE];
        chargeProp.activeTicks = activeTicks;
        chargeProp.calcAndUpdateCurrState(chargeProp.getMin(), HORIZON_SIZE, consumption, IS_FROM_START);
        assertPropertyState(chargeProp, 0 , HORIZON_SIZE, ON_DELTA, OFF_DELTA);
    }

    @Test
    /**
     * tested method calcAndUpdateCurrState
     * checks if the minimal value is kept until activation
     * initial state = 0
     * states: off delta = -10, on delta = +20
     * should have 60 before tick 10, ticks 7-9 were chosen to be on, isFromStart argument is true
     */
    public void calcAndUpdateCurrState_minimalPassiveValueIsKeptUntilActivation()
    {
        final double TARGET_TICK = 10;
        final boolean IS_FROM_START = false;
        final double ON_DELTA = 20;
        final double OFF_DELTA = -10;
        final ArrayList<Integer> activeTicks = new ArrayList<>();
        activeTicks.add(7);
        activeTicks.add(8);
        activeTicks.add(9);

        chargeProp.setTargetTick(TARGET_TICK);
        chargeProp.setDeltaWhenWork(ON_DELTA);
        chargeProp.setDeltaWhenWorkOffline(OFF_DELTA);
        Actuator tesla_s = new Actuator("Tesla_S", "electric_vehicle", "room",
                Arrays.asList(new Action("off", 0,
                                Arrays.asList(new Effect("charge", OFF_DELTA))),
                        new Action("charge_48a", 10,
                                Arrays.asList(new Effect("charge", ON_DELTA)))));
        chargeProp.setActuator(tesla_s);
        double[] consumption = new double[HORIZON_SIZE];
        chargeProp.activeTicks = activeTicks;
        chargeProp.calcAndUpdateCurrState(chargeProp.getMin(), HORIZON_SIZE, consumption, IS_FROM_START);
        double currentCharge = chargeProp.getCachedSensorState();
        for (int i = 0; i < 11; i++)
        {
            if (chargeProp.activeTicks.contains(i))
            {
                currentCharge += ON_DELTA;
            }
            else
            {
                currentCharge += OFF_DELTA;
            }


        }
        Assert.assertTrue(currentCharge >= chargeProp.getMin());
    }

    @Test
    /**
     * tested method calcAndUpdateCurrState
     * checks if the minimal value is kept between activations
     * initial state = 0
     * states: off delta = -10, on delta = +20
     * should have 60 before tick 10,
     * ticks {1,8,9} were chosen to be on, isFromStart argument is true
     */
    public void calcAndUpdateCurrState_bottomPartition_minimalPassiveValueIsKeptBetweenActivations()
    {
        final boolean IS_FROM_START = true;
        calcAndUpdateCurrState_minimalPassiveValueIsKeptBetweenActivations(IS_FROM_START);
    }

    @Test
    /**
     * tested method calcAndUpdateCurrState
     * checks if the minimal value is kept between activations
     * initial state = 0
     * states: off delta = -10, on delta = +20
     * should have 60 before tick 10,
     * ticks {1,8,9} were chosen to be on, isFromStart argument is true
     */
    public void calcAndUpdateCurrState_topPartition_minimalPassiveValueIsKeptBetweenActivations()
    {
        final boolean IS_FROM_START = false;
        calcAndUpdateCurrState_minimalPassiveValueIsKeptBetweenActivations(IS_FROM_START);
    }

    private void calcAndUpdateCurrState_minimalPassiveValueIsKeptBetweenActivations(boolean bottomPivotSelected)
    {
        final double TARGET_TICK = 10;
        final double ON_DELTA = 20;
        final double OFF_DELTA = -10;
        final ArrayList<Integer> activeTicks = new ArrayList<>();
        activeTicks.add(1);
        activeTicks.add(8);
        activeTicks.add(9);

        chargeProp.setTargetTick(TARGET_TICK);
        chargeProp.setDeltaWhenWork(ON_DELTA);
        chargeProp.setDeltaWhenWorkOffline(OFF_DELTA);
        Actuator tesla_s = new Actuator("Tesla_S", "electric_vehicle", "room",
                Arrays.asList(new Action("off", 0,
                                Arrays.asList(new Effect("charge", OFF_DELTA))),
                        new Action("charge_48a", 10,
                                Arrays.asList(new Effect("charge", ON_DELTA)))));
        chargeProp.setActuator(tesla_s);
        double[] consumption = new double[HORIZON_SIZE];
        chargeProp.activeTicks = activeTicks;

        double targetTickToCount = 9;
        chargeProp.calcAndUpdateCurrState(chargeProp.getTargetValue(), targetTickToCount, consumption, bottomPivotSelected);
        Assert.assertTrue(chargeProp.getSensor().getCurrentState() <= chargeProp.getMax());
        Assert.assertTrue(chargeProp.getSensor().getCurrentState() >= chargeProp.getMin());
        Assert.assertTrue(chargeProp.getSensor().getCurrentState() >= chargeProp.getTargetValue());


    }

    /**
     *  checks that for the whole horizon specified by start/end ticks arguments the property state is valid
     * @param chargeProp property under assertion
     * @param startTick start tick index for the interval that is asserted
     * @param endTick end tick index for the interval that is asserted
     */
    private static void assertPropertyState(PropertyWithData chargeProp,
                                            int startTick,
                                            int endTick,
                                            double onDelta,
                                            double offDelta)
    {
        double currentCharge = chargeProp.getCachedSensorState();
        for (int i = startTick; i < chargeProp.activeTicks.size(); i++)
        {
            if (chargeProp.activeTicks.contains(i))
            {
                currentCharge += onDelta;
            }
            else
            {
                currentCharge += offDelta;
            }
            Assert.assertTrue(String.format("property state %f should be greater than the minimal value %f",
                    currentCharge,
                    chargeProp.getMin()),
                    chargeProp.getMin() < currentCharge);
            Assert.assertTrue(String.format("property state %f should not be greater than the maximal value %f",
                    currentCharge,
                    chargeProp.getMax()),
                    chargeProp.getMax() >= currentCharge);

        }
    }

    /////////////////////////////////////////
    // updateValueToSensor
    ////////////////////////////////////////

    /**
     * method under test - updateValueToSensor
     * checks if the additional activations are actually added
     */
    @Test
    public void updateValueToSensor_additionRequired_newActivationAreAdded()
    {
        final double errorMargin = 0.0001;
        Assert.assertEquals(3, this.chargeProp.activeTicks.size());
        double[] consumption = new double[HORIZON_SIZE];
        this.chargeProp.updateValueToSensor(consumption, 60, 2, 2, false);
        Assert.assertEquals(5, this.chargeProp.activeTicks.size());
        Assert.assertEquals(100, this.chargeProp.getSensor().getCurrentState(), errorMargin);
        double consumptionSum = 0;
        for (double d : consumption)
        {
            consumptionSum += d;
        }
        Assert.assertEquals(20, consumptionSum, errorMargin);
    }

    /**
     * method under test - updateValueToSensor
     * checks no additional activations were added
     */
    @Test
    public void updateValueToSensor_additionNotRequired_noNewActivationAdded()
    {
        final double errorMargin = 0.0001;
        Assert.assertEquals(3, this.chargeProp.activeTicks.size());
        double[] consumption = new double[HORIZON_SIZE];
        this.chargeProp.updateValueToSensor(consumption, 60, 0, 2, false);
        Assert.assertEquals(3, this.chargeProp.activeTicks.size());
        Assert.assertEquals(60, this.chargeProp.getSensor().getCurrentState(), errorMargin);
        double consumptionSum = 0;
        for (double d : consumption)
        {
            consumptionSum += d;
        }
        Assert.assertEquals(0, consumptionSum, errorMargin);
    }

}