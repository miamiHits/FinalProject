package FinalProject.BL.DataCollection;

import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.statistics.DefaultStatisticalCategoryDataset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatisticsHandler {

    private List<AlgorithmProblemResult> experimentResultsNotSort;
    private Map<String, List<AlgorithmProblemResult>> experimentResults = new HashMap<>();
    private int ITER_NUM;
    public StatisticsHandler (List<AlgorithmProblemResult> experimentResults)
    {
        this.experimentResultsNotSort = experimentResults;
        ITER_NUM = experimentResults.get(0).getAvgPricePerIteration().size();
        sortResultsByAlgorithm();
    }

    private void sortResultsByAlgorithm()
    {
         for (AlgorithmProblemResult algo : this.experimentResultsNotSort)
         {
             String algoName = algo.getAlgorithm();
             if (!experimentResults.containsKey(algoName))
             {
                 experimentResults.put(algoName, new ArrayList<>());
                 List<AlgorithmProblemResult> tempLst = experimentResults.get(algoName);
                 tempLst.add(algo);
                 experimentResults.put(algoName, tempLst);
             }
             else{
                 List<AlgorithmProblemResult> tempLst = experimentResults.get(algoName);
                 tempLst.add(algo);
                 experimentResults.put(algoName, tempLst);
             }
         }
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
       for(Map.Entry<String, List<AlgorithmProblemResult>> entry : experimentResults.entrySet())
       {
           int size = entry.getValue().size();
           double average =0;
           double [] arr = new double [size];
           for (int j=0; j<=ITER_NUM; j++)
           {
               for (int i=0; i<size; i++)
               {
                   switch (command)
                   {
                       case LowestPrice:
                           arr[i] = entry.getValue().get(i).getLowestCostForAgentInBestIteration().get(j);
                           break;
                       case HighestPrice:
                           arr[i] = entry.getValue().get(i).getHighestCostForAgentInBestIteration().get(j);
                           break;
                       case TotalConsumption:
                           arr[i] = entry.getValue().get(i).getTotalGradePerIteration().get(j);
                           break;
                   }

                   average+= arr[i];
               }
               dataset.add(average/size, calculateSD(arr), entry.getKey(), new Integer(j));
           }
       }
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

    private enum graphType{
        LowestPrice,
        HighestPrice,
        TotalConsumption

    }

}
