package FinalProjectTests.BL.DataCollection;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import FinalProject.BL.DataCollection.*;

public class PowerConsumptionUtilsTest {

    private List<double[]> schedules;
    private double[] priceScheme;
    private double cSum;
    private final double AC = PowerConsumptionUtils.getAC();
    private final double AE = PowerConsumptionUtils.getAE();

    @Before
    public void setUp() throws Exception
    {
        org.apache.log4j.BasicConfigurator.configure();

        priceScheme = new double[]{0.198, 0.198, 0.198, 0.198, 0.225, 0.225, 0.249, 0.849, 0.849, 0.225, 0.225, 0.198}; //taken from dm_7_1_2
        schedules = new ArrayList<>();
        schedules.add(new double[] {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1});
        schedules.add(new double[] {2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2});
        schedules.add(new double[] {4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4});

        cSum = 0;
        for (int i = 0; i < priceScheme.length; i++)
        {
            for (double[] arr : schedules)
            {
                cSum += arr[i] * priceScheme[i];
            }
        }

        cSum *= AC;
    }

    @After
    public void tearDown() throws Exception
    {
        schedules = null;
        priceScheme = null;
        cSum = 0;
    }

    @Test
    public void calculateCSumGood() throws Exception
    {
        double res = PowerConsumptionUtils.calculateCSum(schedules, priceScheme);
        Assert.assertEquals(cSum, res, 0);
    }

    @Test
    public void calculateCSumNullSchedulesBad() throws Exception
    {
        double res = PowerConsumptionUtils.calculateCSum(null, priceScheme);
        Assert.assertEquals(-1, res, 0);
    }

    @Test
    public void calculateCSumNullPriceSchemeBad() throws Exception
    {
        double res = PowerConsumptionUtils.calculateCSum(schedules, null);
        Assert.assertEquals(-1, res, 0);
    }

    @Test
    public void calculateEPeakGood() throws Exception
    {
        double[] oldSched = schedules.get(0);
        schedules.remove(0);
        double[] newSched = {3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3};

        //3.837 = oldSched sum * priceScheme, 11.511 = newSched sum * priceScheme
        double replacedCSum = (cSum - 3.837 * AC + 11.511 * AC);
        double newEPeak = (AE * 12 * (4 + 9 + 16));
        double expected = replacedCSum + newEPeak;
        double res = PowerConsumptionUtils.calculateTotalConsumptionWithPenalty(cSum, newSched, oldSched, schedules, priceScheme);

        Assert.assertEquals(expected, res, 0);
    }

    @Test
    public void calculateEPeakNullOthersBad() throws Exception
    {
        double[] oldSched = schedules.get(0);
        schedules.remove(0);
        double[] newSched = {3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3};

        double res = PowerConsumptionUtils.calculateTotalConsumptionWithPenalty(cSum, newSched, oldSched, null, priceScheme);

        Assert.assertEquals(-1, res, 0);
    }

    @Test
    public void calculateEPeakNewSchedTooLongBad() throws Exception
    {
        double[] oldSched = schedules.get(0);
        schedules.remove(0);
        double[] newSched = {3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 1}; //len = 13 > 12 = priceScheme.length

        double res = PowerConsumptionUtils.calculateTotalConsumptionWithPenalty(cSum, newSched, oldSched, schedules, priceScheme);

        Assert.assertEquals(-1, res, 0);
    }

    @Test
    public void calculateEPeakNewSchedTooShortBad() throws Exception
    {
        double[] oldSched = schedules.get(0);
        schedules.remove(0);
        double[] newSched = {3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3}; //len = 11 < 12 = priceScheme.length

        double res = PowerConsumptionUtils.calculateTotalConsumptionWithPenalty(cSum, newSched, oldSched, schedules, priceScheme);

        Assert.assertEquals(-1, res, 0);
    }

}