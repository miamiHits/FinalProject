package FinalProject.DAL;

import FinalProject.BL.Agents.SmartHomeAgentBehaviour;
import FinalProject.BL.DataObjects.Problem;

import java.io.IOException;
import java.util.List;

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
    public List<SmartHomeAgentBehaviour> getAlgorithms(List<String> algoNames)
    {
        return algoLoader.loadAlgorithms(algoNames);
    }

    @Override
    public void addAlgorithmToSystem(String path, String name)
            throws IllegalAccessException, IOException, InstantiationException
    {
        algoLoader.addAlgoToSystem(path, name);
    }
}
