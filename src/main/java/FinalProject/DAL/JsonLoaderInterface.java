package FinalProject.DAL;

import FinalProject.BL.DataObjects.Device;
import FinalProject.BL.ProblemLoadResult;

import java.util.List;
import java.util.Map;

public interface JsonLoaderInterface {

    ProblemLoadResult loadProblems(List<String> problemNames);
    List<String> getAllProblemNames();
    Map<Integer, List<Device>> loadDevices();
}
