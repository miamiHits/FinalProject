package FinalProject.DAL;

import FinalProject.BL.Agents.SmartHomeAgentBehaviour;
import FinalProject.BL.AlgoAddResult;
import FinalProject.BL.DataCollection.AlgorithmProblemResult;
import FinalProject.BL.ProblemLoadResult;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class DataAccessController implements DataAccessControllerInterface{

    private JsonLoaderInterface jsonLoader;
    private AlgoLoaderInterface algoLoader;
    private FileSaverInterface fileSaver;

    public DataAccessController(JsonLoaderInterface jsonLoader, AlgoLoaderInterface algoLoader)
    {
        this.jsonLoader = jsonLoader;
        this.algoLoader = algoLoader;
        fileSaver = new CsvHandler();
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
    public ProblemLoadResult getProblems(List<String> problemNames)
    {
        return jsonLoader.loadProblems(problemNames);
    }

    @Override
    public List<String> getAvailableAlgorithms()
    {
        return algoLoader.getAllAlgoNames();
    }

    @Override
    public List<String> getAvailableProblems()
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

    @Override
    public boolean saveResults(Map<String, List<Double>> powerConsumption, Map<String, List<Double>> totalPowerConsumptionAnyTime, Map<String, List<Double>> highestAgent, Map<String, List<Double>> lowestAgent, Map<String, List<Long>> averageTimePerIter, Map<String, Long> totalMessagesSize, Map<String, Long> totalAverageMessages, List<AlgorithmProblemResult> experimentResults) {
        fileSaver.setTotalPowerConsumption(powerConsumption);
        fileSaver.setAverageTimePerIter(averageTimePerIter);
        fileSaver.setTotalPowerConsumptionAnyTime(totalPowerConsumptionAnyTime);
        fileSaver.setHighestAgent(highestAgent);
        fileSaver.setLowestAgent(lowestAgent);
        fileSaver.setTotalMessagesSize(totalMessagesSize);
        fileSaver.setTotalAverageMessages(totalAverageMessages);


        try {
            fileSaver.saveExpirmentResult(experimentResults);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
