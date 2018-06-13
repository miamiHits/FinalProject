package FinalProject.DAL;

import FinalProject.BL.DataCollection.AlgorithmProblemResult;

import java.io.IOException;
import java.util.List;
import java.util.Map;


public interface FileSaverInterface {

    void saveExpirmentResult(List<AlgorithmProblemResult> problemResults) throws IOException;
    void setTotalPowerConsumption(Map<String, List<Double>> totalPowerConsumption);
    void setAverageTimePerIter(Map<String,List<Long>> averageTimePerIter);
    void setTotalPowerConsumptionAnyTime(Map<String,List<Double>> totalPowerConsumptionAnyTime);
    void setHighestAgent(Map<String,List<Double>> highestAgent);
    void setLowestAgent(Map<String,List<Double>> lowestAgent);
    void setTotalAverageMessages(Map<String,Long> totalAverageMessages);
    void setTotalMessagesSize(Map<String,Long> totalMessagesSize);
}