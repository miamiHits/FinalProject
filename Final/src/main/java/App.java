import FinalProject.BL.Agents.DSA;
import FinalProject.DAL.*;
import FinalProject.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        JsonLoaderInterface jsonLoader = new JsonLoader("Final\\src\\test\\testResources\\jsons");
        AlgoLoaderInterface algorithmLoader = new AlgorithmLoader("Final\\target\\classes\\FinalProject\\BL\\Agents");
        DataAccessController dal = new DataAccessController(jsonLoader, algorithmLoader);
        Service service = new Service(dal);
        List<String> algoList = new ArrayList<>();
        algoList.add(DSA.class.getName());
        service.addAlgorithmsToExperiment(algoList, 1);
        List<String> problem = new ArrayList<>();
        problem.add("dm_7_1_2");
        service.addProblemsToExperiment(problem);
        service.runExperiment();
        try {
            Thread.sleep(10000000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
