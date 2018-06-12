package FinalProject.DAL;

import FinalProject.BL.Agents.SmartHomeAgentBehaviour;
import FinalProject.BL.AlgoAddResult;
import FinalProject.BL.DataCollection.AlgorithmProblemResult;
import FinalProject.BL.ProblemLoadResult;

import java.util.List;
import java.util.Map;

public interface DataAccessControllerInterface {

    ProblemLoadResult getProblems(List<String> problemNames);
    List<String> getAvailableAlgorithms();
    List<String> getAvailableProblems();
    List<SmartHomeAgentBehaviour> getAlgorithms(List<String> algoNames);
    AlgoAddResult addAlgorithmToSystem(String path, String name);
    boolean saveResults(Map<String, List<Double>> powerConsumption, Map<String, List<Double>> totalPowerConsumptionAnyTime, Map<String, List<Double>> highestAgent, Map<String, List<Double>> totalPowerConsumption, Map<String, List<Long>> averageTimePerIter, List<AlgorithmProblemResult> experimentResults);
}
