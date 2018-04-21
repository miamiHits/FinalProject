package FinalProject.BL.DataCollection;

import FinalProject.BL.Experiment;
import org.apache.log4j.Logger;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.statistics.DefaultStatisticalCategoryDataset;

import java.util.*;

public class StatisticsHandler {

    public static int displayedErrorBarsCount = 10;

    private List<AlgorithmProblemResult> experimentResultsNotSort;
    private Map<String, Map<Integer, Long>> probNAlgToTotalTime;
    private Map<String, List<AlgorithmProblemResult>> experimentResults = new HashMap<>();
    private int ITER_NUM;
    private static Logger logger = Logger.getLogger(StatisticsHandler.class);
    public StatisticsHandler(List<AlgorithmProblemResult> experimentResults, Map<String, Map<Integer, Long>>  probToAlgoTotalTime)
    {
        this.experimentResultsNotSort = experimentResults;
        logger.info("experimentResults is:" + experimentResults.toString());
        ITER_NUM = experimentResults.get(0).getAvgPricePerIteration().size();
        this.probNAlgToTotalTime = probToAlgoTotalTime;
        logger.info("probNAlgToTotalTime is:" + probNAlgToTotalTime.toString());

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
           double total;
           double[] arr = new double[size];
           for (int j = 0; j < ITER_NUM; j++) {
               total = 0;
               for (int i = 0; i < size; i++) {
                   switch (command) {
                       case LowestPrice:
                           arr[i] = value.get(i).getLowestCostForAgentInBestIteration().get(j);
                           break;
                       case HighestPrice:
                           arr[i] = value.get(i).getHighestCostForAgentInBestIteration().get(j);
                           break;
                       case TotalConsumption:
                           if(key.equals("SHMGM"))
                           {
                               dataset.add(value.get(i).getBestTotalGradePerIter().get(j), null, "SHMGM best grade", j);

                           }
                           //logger.info("DEBUG YARDEN: entry.getValue().get(i) of TotalConsumption is: " + entry.getValue().get(i).getAvgPricePerIteration().get(j));
                           arr[i] = value.get(i).getTotalGradePerIteration().get(j);
                           break;
                   }

                   total += arr[i];
               }
               //TODO
               //logger.info("DEBUG YARDEN: key is-->" + key);
               if (key.equals("SHMGM"))
               {
                   Number std = j < displayedErrorBarsCount || j % (ITER_NUM / displayedErrorBarsCount) == 0 ? //disaply only displayedErrorBarsCount error bars for each algorithms
                           calculateSD(arr) :
                           null;
                   dataset.add(total / size, std, key, j);
               }
               else{
                   dataset.add(total / size, null, key, j);
               }

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

    public DefaultStatisticalCategoryDataset averageTime()
    {
        DefaultStatisticalCategoryDataset dataset = new DefaultStatisticalCategoryDataset();
        Set<String> algoNames = experimentResults.keySet();
        for (int j = 0; j < ITER_NUM; j++) {
            for (String name : algoNames) {
                int counter=0;
                long totalTime = 0;
                for (Map.Entry<String, Map<Integer, Long>> entry : probNAlgToTotalTime.entrySet()) {
                    if (entry.getKey().contains(name)) {
                        counter++;
                        totalTime += entry.getValue().get(j);
                    }

               }
                //logger.info("DEBUG YARDEN: in stats class. iter " +j+ "took :" + totalTime + "there are " + counter+ " from " + name);
                dataset.add((totalTime/counter), null, name, j);
            }
        }
        return dataset;
    }

    public DefaultCategoryDataset messageSendPerIteration()
    {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Map<String, Integer> messagesToAlgo = new HashMap<>();
        Map<String, Integer> problemsToAlgo = new HashMap<>();
        calcNumOfProblems(messagesToAlgo, problemsToAlgo);


        for(Map.Entry<String, List<AlgorithmProblemResult>> entry : experimentResults.entrySet())
        {
            for (AlgorithmProblemResult res : entry.getValue())
            {
                 int total = 0;
                 for(int i=0; i<ITER_NUM; i++)
                 {
                     total+= res.getTotalMessagesInIter(i).getMsgsNum();
                 }
                  int currTotalMessages = messagesToAlgo.get(entry.getKey());
                 currTotalMessages+=total;
                 messagesToAlgo.put(entry.getKey(), currTotalMessages);
            }
        }

        for(Map.Entry<String, Integer> entry : messagesToAlgo.entrySet())
        {
            int totalMessages = entry.getValue();
            int totalProblems = problemsToAlgo.get(entry.getKey());
            dataset.addValue(totalProblems!=0? totalMessages/totalProblems : totalMessages, entry.getKey(), "Messages Per Iteration");
        }
        return dataset;

    }

    public DefaultCategoryDataset messagesSize()
    {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Map<String, Integer> messagesToAlgo = new HashMap<>();
        Map<String, Integer> problemsToAlgo = new HashMap<>();

        calcNumOfProblems(messagesToAlgo, problemsToAlgo);
        for(Map.Entry<String, List<AlgorithmProblemResult>> entry : experimentResults.entrySet())
        {
            for (AlgorithmProblemResult res : entry.getValue())
            {
                long total = 0;
                for(int i=0; i<ITER_NUM; i++)
                {
                    total+= res.getTotalMessagesInIter(i).getMsgsSize();
                }
                int currTotalMessages = messagesToAlgo.get(entry.getKey());
                currTotalMessages+=total;
                messagesToAlgo.put(entry.getKey(), currTotalMessages);
            }
        }

        for(Map.Entry<String, Integer> entry : messagesToAlgo.entrySet())
        {
            int totalMessages = entry.getValue();
            int totalProblems = problemsToAlgo.get(entry.getKey());
            dataset.addValue(totalProblems!=0? totalMessages/totalProblems : totalMessages, entry.getKey(), "Messages Per Iteration");
        }
        return dataset;
    }

    private void calcNumOfProblems(Map<String, Integer> messagesToAlgo, Map<String, Integer> problemsToAlgo)
    {
        for(int j=0; j< experimentResultsNotSort.size(); j++)
        {
            String name= experimentResultsNotSort.get(j).getAlgorithm();
            if (!messagesToAlgo.containsKey(name))
            {
                messagesToAlgo.put(name, 0);
            }
            if (!problemsToAlgo.containsKey(name))
            {
                problemsToAlgo.put(name, 1);
            }
            else
            {

                int currNumberOfProblems = problemsToAlgo.get(name);
                currNumberOfProblems++;
                problemsToAlgo.put(name, currNumberOfProblems);
            }
        }
    }

    private enum graphType{
        LowestPrice,
        HighestPrice,
        TotalConsumption

    }

}
