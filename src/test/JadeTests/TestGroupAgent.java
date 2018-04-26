package JadeTests;

import jade.core.Agent;
import jade.wrapper.AgentContainer;
import jade.wrapper.ControllerException;
import test.common.TestException;
import test.common.TestGroup;
import test.common.TesterAgent;

import java.io.File;
import java.util.regex.Matcher;

public class TestGroupAgent extends TesterAgent
{

    @Override
    protected TestGroup getTestGroup()
    {
        String testListPath = "jadeTestResources/testList.xml";
        testListPath.replaceAll("/", Matcher.quoteReplacement(Matcher.quoteReplacement(File.separator)));

        TestGroup tg = new TestGroup(testListPath)
        {
            AgentContainer mainContainer;
            // Re-define the initialize() method to perform initializations common to all
            // tests in the group
            public void initialize(Agent a) throws TestException
            {
                // Perform initializations common to all tests in the group
                this.mainContainer = (AgentContainer) a.getArguments()[0];
            }
            // Re-define the shutdown() method to perform clean-up operations common to all
            // tests in the group
            public void shutdown(Agent a) {
                // Perform clean-up operations common to all tests in the group
                try
                {
                    a.clean(true);
                    this.mainContainer.getPlatformController().kill();
                } catch (ControllerException e)
                {
                    e.printStackTrace();
                }
            }

        };
        return tg;
    }

}
