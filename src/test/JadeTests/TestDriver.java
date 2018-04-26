package JadeTests;

import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

public class TestDriver
{

    public static void main(String[] args)
    {
        try
        {
            Runtime rt = Runtime.instance();
            rt.setCloseVM(true);
            ProfileImpl profile = new ProfileImpl((String)null, 1099, (String)null);
            AgentContainer mainContainer = rt.createMainContainer(profile);
            Object[] testerArgs = new Object[1];
            testerArgs[0] = mainContainer;
                AgentController testSuite = mainContainer.createNewAgent(TestGroupAgent.class.getName(), TestGroupAgent.class.getName(), testerArgs);
            testSuite.start();
        } catch (StaleProxyException e)
        {
            e.printStackTrace();
        }
    }

}
