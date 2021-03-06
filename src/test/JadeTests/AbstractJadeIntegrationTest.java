package JadeTests;

import FinalProject.BL.Agents.DSA;
import FinalProject.BL.Agents.SmartHomeAgent;
import FinalProject.BL.Agents.SmartHomeAgentBehaviour;
import FinalProject.BL.DataObjects.AgentData;
import FinalProject.BL.DataObjects.Problem;
import FinalProject.BL.Experiment;
import FinalProject.DAL.*;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import test.common.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public abstract class AbstractJadeIntegrationTest extends Test
{
    public enum AgentMessageType
    {
        NONE,
        NO_EPEAK,
        WITH_EPEAK
    }

    public static int MAXIMUM_ITERATIONS = 4;

    public SmartHomeAgentBehaviour algorithm;
    public Problem problem;
    public Agent testAgent;

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
        algoNameList.add(DSA.class.getSimpleName());
        List<String> problemNameList = new ArrayList<>();
        problemNameList.add("dm_7_1_2");
        String jsonPath = "src/test/testResources/jsons";
        jsonPath.replaceAll("/", Matcher.quoteReplacement(File.separator));
        String algorithmsPath = "target/classes/FinalProject/BL/Agents";
        algorithmsPath.replaceAll("/", Matcher.quoteReplacement(File.separator));

        JsonLoaderInterface jsonLoader = new JsonLoader(jsonPath);
        AlgoLoaderInterface algorithmLoader = new AlgorithmLoader(algorithmsPath);
        DataAccessController dal = new DataAccessController(jsonLoader, algorithmLoader);
        algorithm =  dal.getAlgorithms(algoNameList).get(0);
        problem = dal.getProblems(problemNameList).getProblems().get(0);
        Experiment.maximumIterations = MAXIMUM_ITERATIONS;
    }

    /**
     * invoked by the first behaviour created and returned by the Test.load(Agent) method
     * used for stating the rest of the agents in the simulator
     * @param initializationAgent
     */
    public void initializeAgents(Agent initializationAgent, SmartHomeAgentBehaviour algorithm, Object[] optionalArgs)
    {
        try
        {
            initializeAgents(initializationAgent, algorithm, problem.getAgentsData(), optionalArgs);
        }
        catch(Exception e)
        {
            failed("could not initialize agents");
            return;
        }
    }

    public void initializeAgents(Agent initializationAgent, SmartHomeAgentBehaviour algorithm, List<AgentData> agentList, Object[] optionalArgs)
    {
        try
        {
            List<AgentController> agentControllers = new ArrayList<>();
            int optionalArgsCount = optionalArgs != null ? optionalArgs.length : 0;
            for (AgentData agentData : agentList)
            {
                Object[] agentInitializationArgs = new Object[4 + optionalArgsCount];
                agentInitializationArgs[0] = algorithm.cloneBehaviour();
                agentInitializationArgs[1] = agentData;
                agentInitializationArgs[2] = algorithm.getBehaviourName();
                agentInitializationArgs[3] = problem.getId();
                for (int i = 0; i < optionalArgsCount; i++)
                {
                    agentInitializationArgs[4 + i] = optionalArgs[i];
                }
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


    public void notifyTestFailed(String message)
    {
        failed(message);
    }

    public void notifyTestPassed(String message)
    {
        passed(message);
    }


}