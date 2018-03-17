package FinalProject;

import FinalProject.DAL.*;
import FinalProject.Service;
import FinalProject.PL.UiHandler;

import java.io.File;
import java.util.regex.Matcher;

public class App
{
    public static void main( String[] args )
    {
        org.apache.log4j.BasicConfigurator.configure();

        String jsonPath = "src/test/testResources/jsons";
        jsonPath.replaceAll("/", Matcher.quoteReplacement(Matcher.quoteReplacement(File.separator)));
        String algorithmsPath = "target/classes/FinalProject/BL/Agents";
        jsonPath.replaceAll("/", Matcher.quoteReplacement(Matcher.quoteReplacement(File.separator)));

        JsonLoaderInterface jsonLoader = new JsonLoader(jsonPath);
        AlgoLoaderInterface algorithmLoader = new AlgorithmLoader(algorithmsPath);
        DataAccessController dal = new DataAccessController(jsonLoader, algorithmLoader);
        Service service = new Service(dal);
        UiHandler ui = new UiHandler(service);
        ui.showMainScreen();
    }
}
