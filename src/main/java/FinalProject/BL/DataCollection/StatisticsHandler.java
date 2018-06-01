package FinalProject.BL.DataCollection;

import org.apache.log4j.Logger;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.statistics.DefaultStatisticalCategoryDataset;

import java.util.*;

public class StatisticsHandler {

    private static final int displayedErrorBarsCount = 10;
    private List<AlgorithmProblemResult> experimentResultsNotSort;
    private Map<String, Map<Integer, Long>> probNAlgToTotalTime;
    private Map<String, List<AlgorithmProblemResult>> experimentResults = new HashMap<>();
    private int ITER_NUM;
    private static Logger logger = Logger.getLogger(StatisticsHandler.class);
    public StatisticsHandler(List<AlgorithmProblemResult> experimentResults, Map<String, Map<Integer, Long>>  probToAlgoTotalTime)
    {
        this.experimentResultsNotSort = experimentResults;
        ITER_NUM = experimentResults.get(0).getTotalGradePerIteration().size();
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

    public Map<String, List<Double>> getTotalPowerConsumption()
    {
        Map<String, List<Double>> res = new HashMap<>();

        experimentResults.forEach((String key, List<AlgorithmProblemResult> value) -> {
            List<Double> iterRes = new ArrayList<>();
            int size = value.size();
            double total;
            double[] arr = new double[size];
            for (int j = 0; j < ITER_NUM; j++) {
                total = 0;
                for (int i = 0; i < size; i++) {
                    Map<Integer, Double> totalGradePerIteration = value.get(i).getTotalGradePerIteration();
                    if (totalGradePerIteration.containsKey(j)) {
                        arr[i] = totalGradePerIteration.get(j);
                        total += arr[i];
                    }
                }

                iterRes.add(total/size);
            }
            res.put(key, iterRes);
        });
        return res;
    }

    public DefaultStatisticalCategoryDataset totalConsumption(String algoName)
    {
        DefaultStatisticalCategoryDataset dataset = new DefaultStatisticalCategoryDataset();
        calcDataSet(graphType.TotalConsumption, dataset, algoName);
        return dataset;
    }

    public DefaultStatisticalCategoryDataset totalConsumptionAnyTime(String algoName)
    {
        DefaultStatisticalCategoryDataset dataset = new DefaultStatisticalCategoryDataset();
        calcDataSet(graphType.TotalConsumptionAnyTime, dataset, algoName);
        return dataset;
    }

    public DefaultStatisticalCategoryDataset lowestAgent(String algoName)
    {
        DefaultStatisticalCategoryDataset dataset = new DefaultStatisticalCategoryDataset();
        calcDataSet(graphType.LowestPrice, dataset, algoName);
        return dataset;
    }

    public DefaultStatisticalCategoryDataset highestAgent(String algoName)
    {
        DefaultStatisticalCategoryDataset dataset = new DefaultStatisticalCategoryDataset();
        calcDataSet(graphType.HighestPrice, dataset, algoName);
        return dataset;
    }

    private void calcDataSet(graphType command, DefaultStatisticalCategoryDataset dataset, String algoName)
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
                           arr[i] =  value.get(i).getLowestCostForAgentInBestIteration().get(j);
                           break;
                       case HighestPrice:
                           arr[i] =  value.get(i).getHighestCostForAgentInBestIteration().get(j);
                           break;
                       case TotalConsumption:
                           arr[i] =  value.get(i).getTotalGradePerIteration().get(j);
                           break;
                       case TotalConsumptionAnyTime:
                           arr[i] = value.get(i).getBestTotalGradePerIter().get(j);
                   }

                   total += arr[i];
               }

               Number std = j < displayedErrorBarsCount || j % (ITER_NUM / displayedErrorBarsCount) == 0 ? calculateSD(arr) : null;
               if (command ==graphType.TotalConsumptionAnyTime)
               {

                   if (key.equals(algoName))
                   {
                       dataset.add(total/ size, std, key + " anytime", j);

                   }
                   else{
                       dataset.add(total/ size, null, key+ " anytime", j);
                   }
               }
               else{
                   if (key.equals(algoName))
                   {
                       dataset.add(total/ size, std, key, j);

                   }
                   else{
                       dataset.add(total/ size, null, key, j);
                   }
               }

           }
       });
   }

    public static double calculateSD(double[] numArray)
    {
        if (numArray.length == 1) {
            return 0.0;
        }
        
        double sum = 0.0;
        double standardDeviation = 0.0;

        for(double num : numArray) {
            sum += num;
        }

        double mean = sum/numArray.length;

        for(double num: numArray) {
            standardDeviation += Math.pow(num - mean, 2);
        }

        return Math.sqrt(standardDeviation/numArray.length);
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
                    if (entry.getKey().contains(name) && entry.getValue().containsKey(j)) {
                        counter++;
                        totalTime += entry.getValue().get(j);
                    }
               }
                //logger.info("DEBUG YARDEN: in stats class. iter " +j+ "took :" + totalTime + "there are " + counter+ " from " + name);
                if (counter > 0) {
                    dataset.add((totalTime/counter), null, name, j);
                }
                else {
                    dataset.add((totalTime), null, name, j);
                    logger.warn("counter was 0!");
                }
            }
        }
        return dataset;
    }

    public DefaultCategoryDataset messageSendPerIteration()
    {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Map<String, Long> messagesToAlgo = new HashMap<>();
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
                  long currTotalMessages = messagesToAlgo.get(entry.getKey());
                 currTotalMessages+=total;
                 messagesToAlgo.put(entry.getKey(), currTotalMessages);
            }
        }

        for(Map.Entry<String, Long> entry : messagesToAlgo.entrySet())
        {
            long totalMessages = entry.getValue();
            int totalProblems = problemsToAlgo.get(entry.getKey());
            dataset.addValue(totalProblems!=0? totalMessages/totalProblems : totalMessages, entry.getKey(), "Messages Per Iteration");
        }
        return dataset;

    }

    public DefaultCategoryDataset messagesSize()
    {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Map<String, Long> messagesToAlgo = new HashMap<>();
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
                long currTotalMessages = messagesToAlgo.get(entry.getKey());
                currTotalMessages+=total;
                messagesToAlgo.put(entry.getKey(), currTotalMessages);
            }
        }

        for(Map.Entry<String, Long> entry : messagesToAlgo.entrySet())
        {
            Long totalMessages = entry.getValue();
            int totalProblems = problemsToAlgo.get(entry.getKey());
            dataset.addValue(totalProblems!=0? totalMessages/totalProblems : totalMessages, entry.getKey(), "Messages Per Iteration");
        }
        return dataset;
    }

    private void calcNumOfProblems(Map<String, Long> messagesToAlgo, Map<String, Integer> problemsToAlgo)
    {
        for(int j=0; j< experimentResultsNotSort.size(); j++)
        {
            String name= experimentResultsNotSort.get(j).getAlgorithm();
            if (!messagesToAlgo.containsKey(name))
            {
                messagesToAlgo.put(name, 0L);
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
        TotalConsumptionAnyTime, TotalConsumption

    }
}
