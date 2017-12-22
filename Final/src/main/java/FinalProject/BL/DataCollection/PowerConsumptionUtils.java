package FinalProject.BL.DataCollection;

import org.apache.log4j.Logger;

import java.util.List;

public class PowerConsumptionUtils {

    private static final Logger logger = Logger.getLogger(PowerConsumptionUtils.class);
    private static final double AC = 1; //TODO: change to correct val
    private static final double AE = 1; //TODO: change to correct val

    public static double getAC()
    {
        return AC;
    }

    public static double getAE()
    {
        return AE;
    }

    public static double calculateCSum(List<double[]> schedules, double[] priceScheme)
    {
        if (schedules == null || schedules.size() == 0 || priceScheme == null ||
                !allTheSameLength(schedules) || schedules.get(0).length != priceScheme.length)
        {

            logger.warn("Could not calculate Csum.");
            return -1;
        }

        double cSum = 0;
        for (int i = 0; i < priceScheme.length; i++)
        {
            for (double[] arr : schedules)
            {
                cSum += arr[i] * priceScheme[i];
            }
        }

        return cSum * AC;
    }

    public static double calculateEPeak(double cSum, double[] newSchedule, double[] oldSchedule,
                                        List<double[]> otherSchedules, double[] priceScheme)
    {
        if (newSchedule.length == oldSchedule.length && newSchedule.length == priceScheme.length
                && allTheSameLength(otherSchedules))
        {
            cSum = replaceInCSum(cSum, newSchedule, oldSchedule, priceScheme);

            otherSchedules.add(newSchedule);
            double eSqrSum = 0;
            for (double[] sched : otherSchedules)
            {
                for (double aSched : sched)
                {
                    eSqrSum += Math.pow(aSched, 2);
                }
            }
            double ePeak = eSqrSum * AE;
            return cSum + ePeak;
        }
        logger.warn("Could not calculate EPeak.");
        return -1;
    }

    private static double replaceInCSum(double cSum, double[] newSchedule, double[] oldSchedule, double[] priceScheme)
    {
        double oldSchedPrice = getSchedPrice(oldSchedule, priceScheme);
        double newSchedPrice = getSchedPrice(newSchedule, priceScheme);

        return cSum - (oldSchedPrice * AC) + (newSchedPrice * AC);
    }

    private static double getSchedPrice(double[] sched, double[] priceScheme)
    {
        double sum = 0;
        for (int i = 0; i < sched.length; i++)
        {
            sum += sched[i] * priceScheme[i];
        }
        return sum;
    }

    private static boolean allTheSameLength(List<double[]> arrays)
    {
        if (arrays == null || arrays.size() == 0 || arrays.get(0) == null)
        {
            return false;
        }

        int len = arrays.get(0).length;
        for (double[] arr : arrays)
        {
            if (arr == null || arr.length != len)
            {
                return false;
            }
        }
        return true;
    }
}
