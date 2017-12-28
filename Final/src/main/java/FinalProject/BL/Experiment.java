package FinalProject.BL;

import FinalProject.BL.Agents.SmartHomeAgent;
import FinalProject.BL.Agents.SmartHomeAgentBehaviour;
import FinalProject.BL.DataCollection.AlgorithmProblemResult;
import FinalProject.BL.DataCollection.DataCollectionCommunicator;
import FinalProject.BL.Problems.AgentData;
import FinalProject.BL.Problems.Problem;
import FinalProject.Service;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.*;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;

public class Experiment implements ExperimentInterface {
    public static int maximumIterations = 0;
    private Service service;
    private List<Problem> problems;
    private List<SmartHomeAgentBehaviour> algorithms;
    private List<AlgorithmProblemResult> algorithmProblemResults;

    private boolean experimentCompleted = false;
    private CyclicBarrier waitingBarrier;// used by the experiment thread to wait for the current configuration to end before starting a new one
    private static final int WAITING_BARRIER_PARTIES_COUNT = 3;

    private AtomicBoolean experimentConfigurationRunning = new AtomicBoolean(false);
    private AtomicBoolean experimentRunStoppedByUser = new AtomicBoolean(false);
    private AtomicBoolean experimentRunStoppedWithError = new AtomicBoolean(false);

    private static Logger logger = Logger.getLogger(Experiment.class);

    private Thread experimentThread;

    public Experiment(Service service, List<Problem> problems, List<SmartHomeAgentBehaviour> algorithms)
    {
        //TODO gal
        logger.info("experiment created");
        this.service = service;
        this.problems = problems;
        this.algorithms = algorithms;
        this.algorithmProblemResults = new ArrayList<>();
        experimentThread = new Thread(new ExperimentRunnable());
    }

    @Override
    public void runExperiment()
    {
        //TODO gal
        logger.info("starting experiment thread");
        assert this.experimentThread != null : "experiment thread must be initiated";
        this.experimentThread.start();
    }

    // gal: this one should be invoked by the data collection agent notifying all data
    // resulted from the algorithm-problem configuration run was fully processed
    // IMPORTANT - the method is blocking and should be invoked when the data collector has done all that is needed for the current configuration
    @Override
    public void algorithmRunEnded(AlgorithmProblemResult result)
    {
        //TODO gal
        logger.info(String.format("data collector completed processing configuration:\n" +
                        "algorithm - %s\n" +
                        "problem - %s"
                , result.getAlgorithm()
                , result.getProblem()));
        assert result != null : "algorithmRunEnded must be invoked with a non-null result instance";
        assert result.getHighestCostForAgentInBestIteration() >= result.getLowestCostForAgentInBestIteration() :
                "result - in best iteration, the highest cost for an agent must be greater than the lowest one";

        algorithmProblemResults.add(result);

        (new Thread(() ->
        {
            try
            {
                this.waitingBarrier.await();// TODO gal make it non-blocking
            }
            catch (InterruptedException e)
            {
                logger.error("got exception while waiting on algorithmRunEnded",
                        e);
                this.experimentRunStoppedWithError.set(true);
                this.waitingBarrier.reset();// wake the other blocked threads
            }
            catch (BrokenBarrierException e)
            {
                logger.warn("got BrokenBarrierException while waiting on algorithmRunEnded",
                        e);
                //TODO gal is there anything to do here?
                if (!experimentRunStoppedWithError.get() &&
                        !experimentRunStoppedByUser.get())
                {
                    logger.error("exception was thrown while !experimentRunStoppedWithError && !experimentRunStoppedByUser", e);
                }
                boolean assertionCondition = (!experimentRunStoppedWithError.get() && !experimentRunStoppedByUser.get());
                assert assertionCondition : "BrokenBarrierException was thrown while !experimentRunStoppedWithError && !experimentRunStoppedByUser";
            }
        })).start();
    }

    @Override
    public void stopExperiment()
    {
        experimentRunStoppedByUser.set(true);
        this.waitingBarrier.reset();
    }

    private Experiment getCurrentInstance()
    {
        return this;
    }


///////////////////////////////////////////////
//ExperimentRunnable
///////////////////////////////////////////////
    private class ExperimentRunnable implements Runnable, PlatformController.Listener
    {
        private AgentContainer mainContainer;
        private List<AgentController> agentControllers;
        private AgentController dataCollectorController;
        private int aliveAgents = 0;
        private Runtime rt;

        @Override
        public void run() {
            //TODO gal
            try
            {
                initialize();

                for (Problem currentProblem : problems)
                    problemLoop:{
                        for (SmartHomeAgentBehaviour currentAlgorithmBehaviour : algorithms)
                            algorithmLoop:{
                                logger.info(String.format("starting new problem-algorithm configuration:\n" +
                                                "algorithm: %s\n" +
                                                "problem: %s",
                                        currentAlgorithmBehaviour.getBehaviourName(),
                                        currentProblem.getId()));
                                for (AgentData agentData : currentProblem.getAgentsData())
                                {
                                    Object[] agentInitializationArgs = new Object[4];
                                    agentInitializationArgs[0] = currentAlgorithmBehaviour.cloneBehaviour();
                                    agentInitializationArgs[1] = agentData;
                                    agentInitializationArgs[2] = currentAlgorithmBehaviour.getBehaviourName();
                                    agentInitializationArgs[3] = currentProblem.getId();
                                    AgentController agentController = this.mainContainer.createNewAgent(agentData.getName(),
                                            SmartHomeAgent.class.getName(),
                                            agentInitializationArgs);
                                    this.agentControllers.add(agentController);
                                }

                                this.aliveAgents = this.agentControllers.size();
                                if (waitingBarrier.isBroken())
                                {//barrier was broken in the previous run. restart the barrier
                                    waitingBarrier.reset();
                                    waitingBarrier = new CyclicBarrier(Experiment.WAITING_BARRIER_PARTIES_COUNT);
                                }
                                experimentConfigurationRunning.set(true);
                                for (AgentController controller : this.agentControllers)
                                {//start all agents
                                    controller.start();
                                }
                                try
                                {
                                    waitingBarrier.await();
                                }
                                catch (InterruptedException e)
                                {//error
                                    logger.error("experiment thread got exception while waiting on for iteration to end",
                                            e);
                                    experimentRunStoppedWithError.set(true);
                                    waitingBarrier.reset();// wake the other blocked threads
                                }
                                catch (BrokenBarrierException e)
                                {
                                    logger.warn("experiment thread got BrokenBarrierException while waiting for algo-problem run to end",
                                            e);
                                }
                                finally
                                {
                                    experimentConfigurationRunning.set(false);
                                    if (experimentRunStoppedByUser.get() || experimentRunStoppedWithError.get())
                                    {
                                        logger.info("recognized experiment ended");
                                        break problemLoop;
                                    }
                                }
                            }
                    }
                this.dataCollectorController.kill();
                logger.info("experiment runner finished running");
            }
            catch (ControllerException e)
            {
                // end the experiment
                stopRun();
                service.experimentEndedWithError(e);
            }
            if (experimentRunStoppedByUser.get())
            {
                logger.info("Experiment stopped by user");
                killJade();
                //TODO gal discard results
            }
            else if (experimentRunStoppedWithError.get())
            {
                logger.info("Experiment stopped with Error!");
                killJade();
                //TODO gal display error message
            }
            else
            {
                logger.info("Experiment ended");
                killJade();
                service.experimentEnded(algorithmProblemResults);
            }
        }

        private void initialize() throws ControllerException {
            logger.info("initialized jade infrastructure");
            // Get a hold on JADE runtime
            rt = Runtime.instance();

            // Exit the JVM when there are no more containers around
            rt.setCloseVM(true);

            // Create a default profile
            Profile profile = new ProfileImpl(true);

            //has to be created even if not used
            this.mainContainer = rt.createMainContainer(profile);
            this.mainContainer.addPlatformListener(this);
            this.agentControllers = new ArrayList<>();

            waitingBarrier = new CyclicBarrier(Experiment.WAITING_BARRIER_PARTIES_COUNT);

            initializeDataCollector();
        }

        private void initializeDataCollector() throws StaleProxyException
        {
            Map<String, Integer> numOfAgentsInProblems = new HashMap<>();// problem name -> count of agents in the problem
            Map<String, double[]> prices = new HashMap<>();//problem name -> price schema
            for (Problem problem : problems)
            {
                numOfAgentsInProblems.put(problem.getId(), problem.getAgentsData().size());
                prices.put(problem.getId(), problem.getPriceScheme());
            }
            Object[] collectorInitializationArgs = new Object[3];
            collectorInitializationArgs[0] = numOfAgentsInProblems;
            collectorInitializationArgs[1] = prices;
            collectorInitializationArgs[2] = getCurrentInstance();
            this.dataCollectorController = this.mainContainer.createNewAgent(DataCollectionCommunicator.SERVICE_NAME,
                    DataCollectionCommunicator.class.getName(),
                    collectorInitializationArgs);
            this.dataCollectorController.start();
            logger.info("started data collector agent");
        }

        private void stopRun()
        {
            logger.info("experiment was stopped");
            killJade();
//            try
//            {
//                this.mainContainer.kill();
//            }
//            catch (StaleProxyException e)
//            {
//                //ignore the exception
//            }

            //not used for now since container.kill might be a better choice
            // TODO gal remove when surely not needed
            /*
            killAllAgents();
            */
        }

        private void killAllAgents() throws StaleProxyException
        {
            for (AgentController controller : this.agentControllers)
            {
                controller.kill();
            }
        }

        private void killJade()
        {
            try
            {
//                killAllAgents();
                mainContainer.kill();
//                mainContainer = null;
//                rt.shutDown();
            } catch (StaleProxyException e)
            {
                logger.warn("could not kill Jade!");
            }
        }

///////////////////////////////////////////////
//PlatformController.Listener methods
///////////////////////////////////////////////
        @Override
        public void bornAgent(PlatformEvent platformEvent) {
            logger.debug(platformEvent.getAgentGUID() + " agent born");
        }

        @Override
        public void deadAgent(PlatformEvent platformEvent) {
            logger.debug(platformEvent.getAgentGUID() + " agent died");

            this.aliveAgents--;
            if (experimentConfigurationRunning.get() &&
                    this.aliveAgents == 0)
            {//all agents are dead(completed their run)
                logger.info("all agents died, will start running next problem-algorithm configuration once data collector sends results");
                (new Thread(() -> {// instead of blocking the caller block an anonymous thread
                    try
                    {
                        waitingBarrier.await();
                    }
                    catch (InterruptedException e)
                    {//error
                        logger.error("experiment listener thread got exception while waiting on for iteration to end",
                                e);
                        experimentRunStoppedWithError.set(true);
                        waitingBarrier.reset();// wake the other blocked threads
                    }
                    catch (BrokenBarrierException e)
                    {
                        logger.info("got BrokenBarrierException while waiting on algorithmRunEnded",
                                e);
                        if (!experimentRunStoppedWithError.get() &&
                                !experimentRunStoppedByUser.get())
                        {
                            logger.error("exception was thrown while !experimentRunStoppedWithError && !experimentRunStoppedByUser", e);
                        }
                    }
                })).start();
            }
        }

        @Override
        public void startedPlatform(PlatformEvent platformEvent) {
            logger.debug("startedPlatform");

        }

        @Override
        public void suspendedPlatform(PlatformEvent platformEvent) {
            logger.debug("suspendedPlatform");
        }

        @Override
        public void resumedPlatform(PlatformEvent platformEvent) {
            logger.debug("resumedPlatform");
        }

        @Override
        public void killedPlatform(PlatformEvent platformEvent) {
            logger.debug("killedPlatform");
        }
    }

    //TODO gal consider removing this one
    @Override
    public boolean experimentCompleted()
    {
        return this.experimentCompleted;
    }


}
