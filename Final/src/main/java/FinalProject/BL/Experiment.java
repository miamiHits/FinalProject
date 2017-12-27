package FinalProject.BL;

import FinalProject.BL.Agents.SmartHomeAgent;
import FinalProject.BL.Agents.SmartHomeAgentBehaviour;
import FinalProject.BL.DataCollection.AlgorithmProblemResult;
import FinalProject.BL.DataCollection.DataCollector;
import FinalProject.BL.Problems.AgentData;
import FinalProject.BL.Problems.Problem;
import FinalProject.Service;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.*;

import java.util.ArrayList;
import java.util.List;

public class Experiment extends Thread{
    public int numOfIterations = 0;
    private Service service;
    private DataCollector dataCollector;
    private List<Problem> problems;
    private List<SmartHomeAgentBehaviour> algorithms;
    private List<AlgorithmProblemResult> algorithmProblemResults;

    private boolean experimentCPmpleted = false;

    private Thread experimentThread;

    public Experiment(Service service, List<Problem> problems, List<SmartHomeAgentBehaviour> algorithms)
    {
        //TODO gal
        this.service = service;
        this.problems = problems;
        this.algorithms = algorithms;
        this.algorithmProblemResults = new ArrayList<>();
        experimentThread = new Thread(new ExperimentRunnable());
    }

    public void runExperiment()
    {
        //TODO gal
        this.experimentThread.start();
    }

    public void algorithmRunEnded(AlgorithmProblemResult result)
    {
        //TODO gal
        algorithmProblemResults.add(result);
    }

    public void stopExperiment()
    {
        //TODO gal
        try
        {
            this.experimentThread.interrupt();
        }
        finally
        {
            //ignore a possible SecurityException
        }
    }

    private class ExperimentRunnable implements Runnable, PlatformController.Listener
    {
        private AgentContainer mainContainer;
        List<AgentController> agentControllers;


        @Override
        public void run() {
            //TODO gal
            try
            {
                inititializeContainer();

                for (Problem currentProblem : problems)
                {
                    for (SmartHomeAgentBehaviour currentAlgorithmBehaviour : algorithms)
                    {
                        AlgorithmProblemResult result = new AlgorithmProblemResult();
                        for (AgentData agentData : currentProblem.getAgentsData())
                        {
                            Object[] agentInitializationArgs = new Object[2];
                            agentInitializationArgs[0] = currentAlgorithmBehaviour.cloneBehaviour();
                            agentInitializationArgs[1] = agentData;
                            AgentController agentController = this.mainContainer.createNewAgent(agentData.getName(),
                                    SmartHomeAgent.class.getName(),
                                    agentInitializationArgs);
                            this.agentControllers.add(agentController);
                        }

                        for (AgentController controller : this.agentControllers)
                        {//start all agents
                            controller.start();
                        }

                        try {
                            Thread.sleep(100000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        algorithmRunEnded(result);
                    }
                }
            }
            catch (ControllerException e)
            {
                // end the experiment
                stopRun();
                service.experimentEndedWithError(e);
            }
        }

        private void inititializeContainer() throws ControllerException {
            // Get a hold on JADE runtime
            Runtime rt = Runtime.instance();

            // Exit the JVM when there are no more containers around
            rt.setCloseVM(true);

            // Create a default profile
            Profile profile = new ProfileImpl(true);

            //has to be created even if not used
            this.mainContainer = rt.createMainContainer(profile);
            this.mainContainer.addPlatformListener(this);
            this.agentControllers = new ArrayList<>();
        }

        private void stopRun()
        {
            try
            {
                this.mainContainer.kill();
            }
            catch (StaleProxyException e)
            {
                //ignore the exception
            }

            //not used for now since container.kill might be a better choice
            // TODO gal remove when surely not needed
            /*
            for (AgentController controller : this.agentControllers)
            {
                try
                {
                    controller.kill();
                }
                catch (StaleProxyException e)
                {
                    //ignore the exception
                }
            }
            */
        }


        //PlatformController.Listener methods
        @Override
        public void bornAgent(PlatformEvent platformEvent) {
            System.out.println(platformEvent.getAgentGUID() + " born");
        }

        @Override
        public void deadAgent(PlatformEvent platformEvent) {
            System.out.println(platformEvent.getAgentGUID() + " dead");
        }

        @Override
        public void startedPlatform(PlatformEvent platformEvent) {
            System.out.println("startedPlatform");

        }

        @Override
        public void suspendedPlatform(PlatformEvent platformEvent) {
            System.out.println("suspendedPlatform");
        }

        @Override
        public void resumedPlatform(PlatformEvent platformEvent) {
            System.out.println("resumedPlatform");
        }

        @Override
        public void killedPlatform(PlatformEvent platformEvent) {
            System.out.println("killedPlatform");
        }
    }

    public boolean experimentCompleted()
    {
        return this.experimentCPmpleted;
    }


}
