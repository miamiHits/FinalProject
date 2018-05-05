package FinalProject.DAL;

import FinalProject.BL.Agents.SmartHomeAgentBehaviour;
import FinalProject.BL.AlgoAddResult;
import FinalProject.BL.DataObjects.Problem;

import java.util.List;
import java.util.Map;

public class DataAccessController implements DataAccessControllerInterface{

    private JsonLoaderInterface jsonLoader;
    private AlgoLoaderInterface algoLoader;

    public DataAccessController(JsonLoaderInterface jsonLoader, AlgoLoaderInterface algoLoader)
    {
        this.jsonLoader = jsonLoader;
        this.algoLoader = algoLoader;
    }

    public JsonLoaderInterface getJsonLoader()
    {
        return jsonLoader;
    }

    public void setJsonLoader(JsonLoaderInterface jsonLoader)
    {
        this.jsonLoader = jsonLoader;
    }

    public AlgoLoaderInterface getAlgoLoader()
    {
        return algoLoader;
    }

    public void setAlgoLoader(AlgoLoaderInterface algoLoader)
    {
        this.algoLoader = algoLoader;
    }

    @Override
    public List<Problem> getProblems(List<String> problemNames)
    {
        return jsonLoader.loadProblems(problemNames);
    }

    @Override
    public List<String> getAvailableAlgorithms()
    {
        return algoLoader.getAllAlgoNames();
    }

    @Override
    public Map<Integer, List<String>> getAvailableProblems()
    {
        return jsonLoader.getAllProblemNames();
    }

    @Override
    public List<SmartHomeAgentBehaviour> getAlgorithms(List<String> algoNames)
    {
        return algoLoader.loadAlgorithms(algoNames);
    }

    @Override
    public AlgoAddResult addAlgorithmToSystem(String path, String name) {
        return algoLoader.addAlgoToSystem(path, name);
    }
}
