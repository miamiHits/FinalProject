package FinalProject.DAL;

import FinalProject.BL.Problems.Device;
import FinalProject.BL.Problems.Problem;

import java.util.List;
import java.util.Map;

public interface JsonLoaderInterface {

    List<Problem> loadProblems(List<String> problemNames);
    List<String> getAllProblemNames();
    Map<Integer, List<Device>> loadDevices();
}
