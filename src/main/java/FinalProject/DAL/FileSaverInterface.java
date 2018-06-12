package FinalProject.DAL;

import FinalProject.BL.DataCollection.AlgorithmProblemResult;

import java.io.IOException;
import java.util.List;
import java.util.Map;


public interface FileSaverInterface {

    void saveExpirmentResult(List<AlgorithmProblemResult> problemResults) throws IOException;
    void setTotalPowerConsumption(Map<String, List<Double>> totalPowerConsumption);

    void setAverageTimePerIter(Map<String,List<Long>> averageTimePerIter);
}