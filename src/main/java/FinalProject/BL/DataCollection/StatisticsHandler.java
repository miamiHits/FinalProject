package FinalProject.BL.DataCollection;

import FinalProject.BL.Experiment;
import org.apache.log4j.Logger;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.statistics.DefaultStatisticalCategoryDataset;

import java.util.*;

public class StatisticsHandler {

    public static int displayedErrorBarsCount = 10;

    private List<AlgorithmProblemResult> experimentResultsNotSort;
    private Map<String, Long> probNAlgToTotalTime;
    private Map<String, List<AlgorithmProblemResult>> experimentResults = new HashMap<>();
    private int ITER_NUM;
    private static Logger logger = Logger.getLogger(StatisticsHandler.class);
    public StatisticsHandler(List<AlgorithmProblemResult> experimentResults, Map<String, Long> probToAlgoTotalTime)
    {
        this.experimentResultsNotSort = experimentResults;
        ITER_NUM = experimentResults.get(0).getAvgPricePerIteration().size();
        this.probNAlgToTotalTime = probToAlgoTotalTime;
        sortResultsByAlgorithm();
    }

    private void sortResultsByAlgorithm()
    {
        this.experimentResultsNotSort.forEach(algo -> {
            String algoName = algo.getAlgorithm();
            if (!experimentResults.containsKey(algoName)) {
                experimentResults.put(algoName, new ArrayList<>());
                List<AlgorithmProblemResult> tempLst = experimentResults.get(algoName);
                tempLst.add(algo);
                experimentResults.put(algoName, tempLst);
            } else {
                List<AlgorithmProblemResult> tempLst = experimentResults.get(algoName);
                tempLst.add(algo);
                experimentResults.put(algoName, tempLst);
            }
        });
    }

    public DefaultStatisticalCategoryDataset totalConsumption()
    {
        DefaultStatisticalCategoryDataset dataset = new DefaultStatisticalCategoryDataset();
        calcDataSet(graphType.TotalConsumption, dataset);
        return dataset;
    }

    public DefaultStatisticalCategoryDataset lowestAgent()
    {
        DefaultStatisticalCategoryDataset dataset = new DefaultStatisticalCategoryDataset();
        calcDataSet(graphType.LowestPrice, dataset);
        return dataset;
    }

    public DefaultStatisticalCategoryDataset highestAgent()
    {
        DefaultStatisticalCategoryDataset dataset = new DefaultStatisticalCategoryDataset();
        calcDataSet(graphType.HighestPrice, dataset);
        return dataset;
    }


    private void calcDataSet(graphType command, DefaultStatisticalCategoryDataset dataset)
   {
       experimentResults.forEach((String key, List<AlgorithmProblemResult> value) -> {
           int size = value.size();
           double average;
           double[] arr = new double[size];
           for (int j = 0; j < ITER_NUM; j++) {
               average = 0;
               for (int i = 0; i < size; i++) {
                   switch (command) {
                       case LowestPrice:
                           arr[i] = value.get(i).getLowestCostForAgentInBestIteration().get(j);
                           break;
                       case HighestPrice:
                           arr[i] = value.get(i).getHighestCostForAgentInBestIteration().get(j);
                           break;
                       case TotalConsumption:
                           //logger.info("DEBUG YARDEN: entry.getValue().get(i) of TotalConsumption is: " + entry.getValue().get(i).getAvgPricePerIteration().get(j));
                           arr[i] = value.get(i).getTotalGradePerIteration().get(j);
                           break;
                   }

                   average += arr[i];
               }
               Number std = j < displayedErrorBarsCount || j % (ITER_NUM / displayedErrorBarsCount) == 0 ? //disaply only displayedErrorBarsCount error bars for each algorithms
                       calculateSD(arr) :
                       null;
               dataset.add(average / size, std, key, new Integer(j));
           }
       });
   }

    public static double calculateSD(double numArray[])
    {
        double sum = 0.0, standardDeviation = 0.0;

        for(double num : numArray) {
            sum += num;
        }

        double mean = sum/10;

        for(double num: numArray) {
            standardDeviation += Math.pow(num - mean, 2);
        }

        return Math.sqrt(standardDeviation/10);
    }

    public DefaultCategoryDataset averageTime()
    {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Set<String> algoNames = experimentResults.keySet();
        for(String name: algoNames)
        {
           logger.info("DEBUG YARDEN: looking for algo name: " + name);

            int counter=0;
            long totalTime = 0;
            for(Map.Entry<String, Long> entry : probNAlgToTotalTime.entrySet())
            {
                logger.info("DEBUG YARDEN: looking for algoProb name: " + entry.getKey());
                logger.info("DEBUG YARDEN: is it contains: " +entry.getKey().contains(name));
                if (entry.getKey().contains(name))
                {
                    counter++;
                    logger.info("DEBUG YARDEN: the value is: " +entry.getValue());
                    totalTime+=entry.getValue();
                }

            }
            dataset.addValue(totalTime/counter, name, "Iteration Run Time (ms)");
        }
        return dataset;
    }

    public DefaultCategoryDataset messageSendPerIteration()
    {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for(Map.Entry<String, List<AlgorithmProblemResult>> entry : experimentResults.entrySet())
        {
            int total=0;
            for (int i=0; i<ITER_NUM; i++)
            {

            }
        }
        return dataset;
    }

    private enum graphType{
        LowestPrice,
        HighestPrice,
        TotalConsumption

    }

}
