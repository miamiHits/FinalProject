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
        Config.loadConfig(); //TODO check if should be here
        UiHandler ui = new UiHandler();
        ui.showMainScreen();
    }
}
