package FinalProject.DAL;

import FinalProject.BL.DataObjects.Device;
import FinalProject.BL.DataObjects.Problem;

import java.util.List;
import java.util.Map;

public interface JsonLoaderInterface {

    List<Problem> loadProblems(List<String> problemNames);
    Map<Integer, List<String>> getAllProblemNames();
    Map<Integer, List<Device>> loadDevices();
}
