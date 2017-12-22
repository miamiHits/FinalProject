package FinalProject.BL.DataCollection;

import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
        double cSum = 0;
        for (int i = 0; i < priceScheme.length; i++)
        {
            for (double[] arr : schedules)
            {
                cSum += arr[i] * priceScheme[i];
            }
        }

        return cSum * AC;

//        if (allTheSameLength(schedules) && priceScheme.length == schedules.get(0).length)
//        {
//            Optional<double[]> summedSchedulesOpt = sumArrListToOpt(schedules);
//
//            if (summedSchedulesOpt.isPresent())
//            {
//                double[] summedArr = summedSchedulesOpt.get();
//                for (int i = 0; i < summedArr.length; i++)
//                {
//                    summedArr[i] *= priceScheme[i];
//                }
//
//                return AC * sumArray(summedArr);
//            }
//        }
//        logger.warn("calculateCSum could not calculate Csum.");
//        return -1;
    }

    private static Optional<double[]> sumArrListToOpt(List<double[]> schedules)
    {
        return schedules.stream()
                .reduce((sched1, sched2) -> {
                    for (int i = 0; i < sched1.length; i++)
                    {
                        sched1[i] += sched2[i];
                    }
                    return sched1;
                });
    }

    public static double calculateEPeak(double cSum, double[] newSchedule, double[] oldSchedule,
                                        List<double[]> otherSchedules, double[] priceScheme)
    {
        if (newSchedule.length == oldSchedule.length && newSchedule.length == priceScheme.length
                && allTheSameLength(otherSchedules))
        {
            cSum = replaceInCSum(cSum, newSchedule, oldSchedule, priceScheme);

            otherSchedules.add(newSchedule);
            for (double[] sched : otherSchedules)
            {
                for (int i = 0; i < sched.length; i++)
                {
                    sched[i] = Math.pow(sched[i], 2);
                }
            }
            Optional<double[]> consumptionSqrsOpt = sumArrListToOpt(otherSchedules);
            if (consumptionSqrsOpt.isPresent())
            {
                double ePeak = sumArray(consumptionSqrsOpt.get()) * AE;
                return cSum + ePeak;
            }
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

    private static double sumArray(double[] summedArr)
    {
        return Arrays.stream(summedArr).reduce(0, Double::sum);
    }

    private static double getSchedPrice(double[] sched, double[] priceScheme)
    {
        for (int i = 0; i < sched.length; i++)
        {
            sched[i] *= priceScheme[i];
        }
        return sumArray(sched);
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
