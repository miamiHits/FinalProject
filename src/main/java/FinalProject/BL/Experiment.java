package FinalProject.BL;

import FinalProject.BL.Agents.SmartHomeAgent;
import FinalProject.BL.Agents.SmartHomeAgentBehaviour;
import FinalProject.BL.DataCollection.AlgorithmProblemResult;
import FinalProject.BL.DataCollection.DataCollectionCommunicator;
import FinalProject.BL.DataObjects.AgentData;
import FinalProject.BL.DataObjects.Problem;
import FinalProject.PL.UIEntities.ProblemAlgoPair;
import FinalProject.Service;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.*;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class Experiment implements ExperimentInterface {
    public static int maximumIterations = 0;
    private Service service;
    private List<Problem> problems;
    private List<SmartHomeAgentBehaviour> algorithms;
    private List<AlgorithmProblemResult> algorithmProblemResults;
    private Map<Integer, Long> iter2Time = new HashMap<>();
    private Map<String, Map<Integer, Long>> probToAlgoTotalTime = new HashMap<>();
    private boolean experimentCompleted = false;
    private CyclicBarrier waitingBarrier;// used by the experiment thread to wait for the current configuration to end before starting a new one
    private static final int WAITING_BARRIER_PARTIES_COUNT = 3;
    private AtomicBoolean experimentConfigurationRunning = new AtomicBoolean(false);
    private AtomicBoolean experimentRunStoppedByUser = new AtomicBoolean(false);
    private AtomicBoolean experimentRunStoppedWithError = new AtomicBoolean(false);

    private static Logger logger = Logger.getLogger(Experiment.class);
    private long runningTime;
    private int counter=0;

    private Thread experimentThread;

    public Experiment(Service service, List<Problem> problems, List<SmartHomeAgentBehaviour> algorithms)
    {
        logger.info("experiment created");
        this.service = service;
        this.problems = problems;
        this.algorithms = algorithms;
        this.algorithmProblemResults = new ArrayList<>();
        experimentThread = new Thread(new ExperimentRunnable());
        makePairs(problems, algorithms);
    }

    private void makePairs(List<Problem> problems, List<SmartHomeAgentBehaviour> algorithms) {
        for (Problem p : problems) {
            algorithms.stream()
                    .map(algo -> p.getId() + "_" + algo.getBehaviourName())
                    .filter(prob2Algo -> probToAlgoTotalTime.containsKey(prob2Algo))
                    .forEach(prob2Algo -> probToAlgoTotalTime.put(prob2Algo, new HashMap<>()));
        }
    }

    @Override
    public void runExperiment()
    {
        logger.info("starting experiment thread");
        assert this.experimentThread != null : "experiment thread must be initiated";
        this.experimentThread.start();
        this.runningTime = System.currentTimeMillis();
    }

    public void algorithmProblemIterEnded(String algo, String problem) {
        logger.info("Iter ended in <" + algo + "," + problem + ">. Updating with " + (1.0 / maximumIterations) + "%");
        iter2Time.put(this.counter, (System.currentTimeMillis() - this.runningTime));
        counter++;
        this.runningTime = System.currentTimeMillis();
        service.algorithmProblemIterEnded(algo, problem, 1.0f / maximumIterations);
    }

    // gal: this one should be invoked by the data collection agent notifying all data
    // resulted from the algorithm-problem configuration run was fully processed
    // IMPORTANT - operation is non blocking
    @Override
    public void algorithmProblemComboRunEnded(AlgorithmProblemResult result)
    {
        logger.info(String.format("data collector completed processing configuration:\n" +
                        "algorithm - %s\n" +
                        "problem - %s"
                , result.getAlgorithm()
                , result.getProblem()));
        assert result != null : "algorithmProblemComboRunEnded must be invoked with a non-null result instance";

        assert result.getIterationsTillBestPrice() <= Experiment.maximumIterations :
                "result.getIterationsTillBestPrice() was greater than the maximum iteration count";
        iter2Time.put(counter, (System.currentTimeMillis() - this.runningTime));
        algorithmProblemResults.add(result);
        this.probToAlgoTotalTime.put(result.getProblem()+"_"+result.getAlgorithm(), this.iter2Time);
       // for(int i=0; i<counter; i++)
           // logger.debug("iteration " + i + " took :" + iter2Time.get(i));
        this.counter = 0;
        this.iter2Time = new HashMap<>();
        this.runningTime = System.currentTimeMillis();
        service.algorithmProbleComboRunEnded(result.getAlgorithm(), result.getProblem());
        (new Thread(() ->
        {
            try
            {
                this.waitingBarrier.await();
                logger.debug("waiting barrier released");
            }
            catch (InterruptedException e)
            {
                logger.error("got exception while waiting on algorithmProblemComboRunEnded",
                        e);
                this.experimentRunStoppedWithError.set(true);
                this.waitingBarrier.reset();// wake the other blocked threads
            }
            catch (BrokenBarrierException e)
            {
                logger.warn("got BrokenBarrierException while waiting on algorithmProblemComboRunEnded",
                        e);
                //TODO gal is there anything to do here?
                if (!experimentRunStoppedWithError.get() &&
                        !experimentRunStoppedByUser.get())
                {
                    logger.error("exception was thrown while !experimentRunStoppedWithError && !experimentRunStoppedByUser", e);
                    assert false : "barrier was broken without the user stopping the experiment or an error occoured";//the system reached a bad state - should fail the assertion test
                }
            }
        })).start();
    }

    @Override
    public void stopExperiment()
    {
        if (isExperimentRunning())
        {//stop the experiment only when is running
            experimentRunStoppedByUser.set(true);
            this.waitingBarrier.reset();
        }
    }

    private Experiment getCurrentInstance()
    {
        return this;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        Experiment that = (Experiment) o;

        if (!service.equals(that.service))
        {
            return false;
        }
        if (!problems.equals(that.problems))
        {
            return false;
        }
        return algorithms.equals(that.algorithms);

    }

    @Override
    public int hashCode()
    {
        int result = service.hashCode();
        result = 31 * result + problems.hashCode();
        result = 31 * result + algorithms.hashCode();
        return result;
    }

    public boolean isExperimentRunning()
    {
        boolean isExperimentThreadAlive = this.experimentThread != null && this.experimentThread.isAlive();
        return !experimentRunStoppedWithError.get() && !experimentRunStoppedByUser.get() && !experimentCompleted && isExperimentThreadAlive;
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

                                assert currentProblem.getAgentsData().stream()
                                        .map(AgentData::getName)
                                        .distinct()
                                        .collect(Collectors.toList()).size() == currentProblem.getAgentsData().size() :
                                        "some of the agents did not have have unique name";

                                for (AgentData agentData : currentProblem.getAgentsData())
                                {
                                    assert agentData.getName() != null && !agentData.getName().equals("") :
                                            "agent must have a name that is not an empty string";
                                    agentData.setNumOfIterations(maximumIterations);
                                    Object[] agentInitializationArgs = new Object[4];
                                    agentInitializationArgs[0] = currentAlgorithmBehaviour.cloneBehaviour();
                                    agentInitializationArgs[1] = new AgentData(agentData);
                                    agentInitializationArgs[2] = currentAlgorithmBehaviour.getBehaviourName();
                                    agentInitializationArgs[3] = currentProblem.getId();
                                    AgentController agentController = this.mainContainer.createNewAgent(agentData.getName(),
                                            SmartHomeAgent.class.getName(),
                                            agentInitializationArgs);

                                    assert (agentController.getState().getName().equals("Initiated")) :
                                            "new agent state was not == INITIATED after initiated";


                                    this.agentControllers.add(agentController);
                                }

                                assert this.agentControllers
                                        .stream()
                                        .map(ac -> {
                                            try {return ac.getName();}
                                        catch(Throwable t){} return null;})
                                        .distinct()
                                        .filter(name -> name != null)
                                        .collect(Collectors.toList())
                                        .size() == currentProblem.getAgentsData().size() :
                                        "some of the agents did not have have unique name (StaleProxyException might have been thrown)";


//                                this.aliveAgents = this.agentControllers.size();
                                this.aliveAgents = currentProblem.getAgentsData().size();
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
                                    logger.debug("waiting barrier released");
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
                                    if (!experimentRunStoppedWithError.get() &&
                                            !experimentRunStoppedByUser.get())
                                    {
                                        logger.error("exception was thrown while !experimentRunStoppedWithError && !experimentRunStoppedByUser", e);
                                        assert false : "barrier was broken without the user stopping the experiment or an error occoured";//the system reached a bad state - should fail the assertion test
                                    }
                                }
                                finally
                                {
                                    assert waitingBarrier.getNumberWaiting() == 0 :
                                            "no threads should wait on the barrier once algo-problem conf run ended";

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
                service.experimentEnded(algorithmProblemResults, probToAlgoTotalTime);
            }
            assert !experimentConfigurationRunning.get() :
                    "experimentConfigurationRunning should be false when experiment has ended";
        }

        private void initialize() throws ControllerException {
            logger.info("initialized jade infrastructure");
            // Get a hold on JADE runtime
            rt = Runtime.instance();

            // Exit the JVM when there are no more containers around
            rt.setCloseVM(false);


            // Create a default profile
            Profile profile = new ProfileImpl(true);
            profile.setParameter("jade_core_messaging_MessageManager_poolsize", "30"); //size of jade MessageManager's thread pool. default: 5
            profile.setParameter("jade_core_messaging_MessageManager_deliverytimethreshold", "7000"); //jade MessageManager's max time to deliver a message: default: 1000
            profile.setParameter("jade_core_messaging_MessageManager_deliveryStuckTime", "10000"); //jade MessageManager's time after which a message is considered stuck: default: 5000


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

            assert numOfAgentsInProblems.keySet().size() == problems.size() :
                    "not all of the problem agent counts were sent to the data collection agent";
            assert prices.keySet().size() == problems.size() :
                    "not all of the problem agent price schemas were sent to the data collection agent";

            Object[] collectorInitializationArgs = new Object[3];
            collectorInitializationArgs[0] = numOfAgentsInProblems;
            collectorInitializationArgs[1] = prices;
            collectorInitializationArgs[2] = getCurrentInstance();
            this.dataCollectorController = this.mainContainer.createNewAgent(DataCollectionCommunicator.SERVICE_NAME,
                    DataCollectionCommunicator.class.getName(),
                    collectorInitializationArgs);
            assert (this.dataCollectorController.getState().getName().equals("Initiated")) :
                    "data collector state was not == INITIATED after initiated";
            this.dataCollectorController.start();
            logger.info("started data collector agent");
        }

        private void stopRun()
        {
            logger.info("experiment was stopped");
            killJade();
        }

        private void killAllAgents()
        {
            for (AgentController controller : this.agentControllers)
            {
                try
                {
                    if (!controller.getState().getName().equalsIgnoreCase("killed"))
                    {
                        controller.kill();
                    }
                }
                catch (Exception e)
                {
                    logger.error("failed killing an agent", e);
                }
            }
            this.agentControllers = new ArrayList<>();
        }

        private void killJade()
        {
            try
            {
                mainContainer.removePlatformListener(this);
                mainContainer.getPlatformController().kill();
            }
            catch (StaleProxyException e)
            {
                logger.warn("could not kill Jade!");
            }
            catch (ControllerException e)
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
            if (experimentConfigurationRunning.get() && this.aliveAgents == 0)
            {//all agents are dead(completed their run)
                logger.info("all agents died, will start running next problem-algorithm configuration once data collector sends results");
                assert this.agentControllers
                        .stream()
                        .allMatch(ac -> {
                            try {
                                ac.getState();
                                return false;}
                            catch(StaleProxyException e){
                            return true;
                            }}) :
                        "upon ending ";
                this.agentControllers = new ArrayList<>();
                (new Thread(() -> {// instead of blocking the caller block an anonymous thread
                    try
                    {
                        waitingBarrier.await();
                        logger.debug("waiting barrier released");
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
                        logger.info("got BrokenBarrierException while waiting on algorithmProblemComboRunEnded",
                                e);
                        if (!experimentRunStoppedWithError.get() &&
                                !experimentRunStoppedByUser.get())
                        {
                            logger.error("exception was thrown while !experimentRunStoppedWithError && !experimentRunStoppedByUser", e);
                            assert false : "barrier was broken without the user stopping the experiment or an error occoured";//the system reached a bad state - should fail the assertion test
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

}
