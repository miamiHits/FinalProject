package FinalProject.DAL;

import FinalProject.BL.Agents.SmartHomeAgentBehaviour;

import java.io.IOException;
import java.util.List;

public interface AlgoLoaderInterface {

    List<SmartHomeAgentBehaviour> loadAlgorithms(List<String> algoNames);
    List<String> getAllAlgoNames();
    String addAlgoToSystem(String path, String fileName);

}
