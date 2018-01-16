package FinalProject.Experiment;

import FinalProject.BL.Agents.DSA;
import FinalProject.BL.Agents.SmartHomeAgent;
import FinalProject.BL.Agents.SmartHomeAgentBehaviour;
import FinalProject.BL.DataCollection.DataCollectionCommunicator;
import FinalProject.BL.DataObjects.AgentData;
import FinalProject.BL.DataObjects.Problem;
import FinalProject.BL.Experiment;
import FinalProject.DAL.*;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import test.common.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;

public abstract class AbstractJadeIntegrationTest extends Test
{
    protected enum AgentMessageType
    {
        NONE,
        NO_EPEAK,
        WITH_EPEAK
    }

    public static int MAXIMUM_ITERATIONS = 4;
    public static int currentTestIterationNumber = 0;

    protected SmartHomeAgentBehaviour algorithm;
    protected Problem problem;

    public boolean initializationApplied = false;

    @Override
    public Behaviour load(Agent agent) throws TestException
    {
        org.apache.log4j.BasicConfigurator.configure();
        return null;//let the concrete class create the behaviour
    }

    public void clean(Agent a) {
        // Perform test specific clean-up
    }

    public void loadExperimentConfiguration()
    {
        List<String> algoNameList = new ArrayList<>();
        algoNameList.add(DSA.class.getName());
        List<String> problemNameList = new ArrayList<>();
        problemNameList.add("dm_7_1_2");
        JsonLoaderInterface jsonLoader = new JsonLoader("Final\\src\\test\\testResources\\jsons");
        AlgoLoaderInterface algorithmLoader = new AlgorithmLoader("Final\\target\\classes\\FinalProject\\BL\\Agents");
        DataAccessController dal = new DataAccessController(jsonLoader, algorithmLoader);
        algorithm =  dal.getAlgorithms(algoNameList).get(0);
        problem = dal.getProblems(problemNameList).get(0);
        Experiment.maximumIterations = MAXIMUM_ITERATIONS;
    }

    public void initializeAgents(Agent initializationAgent)
    {
        try
        {
            List<AgentController> agentControllers = new ArrayList<>();
            for (AgentData agentData : problem.getAgentsData())
            {
                Object[] agentInitializationArgs = new Object[4];
                agentInitializationArgs[0] = algorithm.cloneBehaviour();
                agentInitializationArgs[1] = agentData;
                agentInitializationArgs[2] = algorithm.getBehaviourName();
                agentInitializationArgs[3] = problem.getId();
                agentControllers.add(initializationAgent.getContainerController().createNewAgent(agentData.getName(),
                        SmartHomeAgent.class.getName(),
                        agentInitializationArgs));
            }

            for (AgentController agentContainer : agentControllers)
            {
                agentContainer.start();
            }
        }
        catch(Exception e)
        {
            failed("could not initialize agents");
            return;
        }
    }


    protected void startRegularDataCollector(Agent initializationAgent) throws StaleProxyException
    {
        Experiment mockExperiment = mock(Experiment.class);

        Experiment.maximumIterations = MAXIMUM_ITERATIONS;

        Map<String, Integer> numOfAgentsInProblems = new HashMap<>();
        Map<String, double[]> prices = new HashMap<>();
        numOfAgentsInProblems.put(problem.getId(), problem.getAgentsData().size());
        prices.put(problem.getId(), problem.getPriceScheme());

        Object[] collectorInitializationArgs = new Object[3];
        collectorInitializationArgs[0] = numOfAgentsInProblems;
        collectorInitializationArgs[1] = prices;
        collectorInitializationArgs[2] = mockExperiment;
        AgentController dataCollectorController = initializationAgent.getContainerController().createNewAgent(DataCollectionCommunicator.SERVICE_NAME,
                DataCollectionCommunicator.class.getName(),
                collectorInitializationArgs);
        dataCollectorController.start();
    }


}