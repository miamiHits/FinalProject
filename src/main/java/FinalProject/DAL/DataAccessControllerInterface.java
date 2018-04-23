package FinalProject.DAL;

import FinalProject.BL.Agents.SmartHomeAgentBehaviour;
import FinalProject.BL.DataObjects.Problem;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface DataAccessControllerInterface {

    List<Problem> getProblems(List<String> problemNames);
    List<String> getAvailableAlgorithms();
    Map<Integer, List<String>> getAvailableProblems();
    List<SmartHomeAgentBehaviour> getAlgorithms(List<String> algoNames);
    String addAlgorithmToSystem(String path, String name);
}
