package FinalProject.DAL;

import FinalProject.BL.Agents.SmartHomeAgentBehaviour;
import FinalProject.BL.AlgoAddResult;

import java.util.List;

public interface AlgoLoaderInterface {

    List<SmartHomeAgentBehaviour> loadAlgorithms(List<String> algoNames);
    List<String> getAllAlgoNames();
    AlgoAddResult addAlgoToSystem(String path, String fileName);

}
