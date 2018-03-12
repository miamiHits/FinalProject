package FinalProject.DAL;

import FinalProject.BL.Agents.SmartHomeAgentBehaviour;
import FinalProject.BL.DataObjects.Problem;

import java.io.IOException;
import java.util.List;

public interface DataAccessControllerInterface {

    List<Problem> getProblems(List<String> problemNames);
    List<String> getAvailableAlgorithms();
    List<SmartHomeAgentBehaviour> getAlgorithms(List<String> algoNames);
    void addAlgorithmToSystem(String path, String name) throws IllegalAccessException, IOException, InstantiationException;
}
