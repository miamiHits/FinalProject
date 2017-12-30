package FinalProject.Experiment;

import jade.core.Agent;
import jade.wrapper.AgentContainer;
import jade.wrapper.ControllerException;
import test.common.TestException;
import test.common.TestGroup;
import test.common.TesterAgent;

public class TestGroupAgent extends TesterAgent
{

    @Override
    protected TestGroup getTestGroup()
    {
        TestGroup tg = new TestGroup("FinalProject\\Experiment\\testList.xml")
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
                    a.doDelete();
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
