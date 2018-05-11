package FinalProject;

import FinalProject.PL.UiHandler;

public class App
{
    public static void main( String[] args )
    {
        org.apache.log4j.BasicConfigurator.configure();
        Config.loadConfig();
        UiHandler ui = new UiHandler();
        ui.showMainScreen();
    }
}
