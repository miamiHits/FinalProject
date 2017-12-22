package FinalProject.BL.DataCollection;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class PowerConsumptionUtilsTest {

    private List<double[]> schedules;
    private double[] priceScheme;
    private double cSum;
    private final double AC = PowerConsumptionUtils.getAC();
    private final double AE = PowerConsumptionUtils.getAE();

    @Before
    public void setUp() throws Exception
    {
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
    public void calculateCSum() throws Exception
    {
        double res = PowerConsumptionUtils.calculateCSum(schedules, priceScheme);
        Assert.assertEquals(cSum, res, 0);
    }

    @Test
    public void calculateEPeak() throws Exception
    {
        double[] oldSched = schedules.get(0);
        schedules.remove(0);
        double[] newSched = {3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3};

        //3.837 = oldSched sum * priceScheme, 11.511 = newSched sum * priceScheme
        double replacedCSum = (cSum - 3.837 * AC + 11.511 * AC);
        double newEPeak = (AE * 12 * (4 + 9 + 16));
        double expected = replacedCSum + newEPeak;
        double res = PowerConsumptionUtils.calculateEPeak(cSum, newSched, oldSched, schedules, priceScheme);

        Assert.assertEquals(expected, res, 0);

    }

}