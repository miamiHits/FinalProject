import FinalProject.DAL.*;
import FinalProject.Service;
import FinalProject.UiHandler;

public class App
{
    public static void main( String[] args )
    {
        org.apache.log4j.BasicConfigurator.configure();

        JsonLoaderInterface jsonLoader = new JsonLoader("src\\test\\testResources\\jsons");
        AlgoLoaderInterface algorithmLoader = new AlgorithmLoader("target\\classes\\FinalProject\\BL\\Agents");
        DataAccessController dal = new DataAccessController(jsonLoader, algorithmLoader);
        Service service = new Service(dal);
        UiHandler ui = new UiHandler(service);
        ui.showMainScreen();
    }
}
