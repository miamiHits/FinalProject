package FinalProject.BL;

import FinalProject.BL.Agents.SmartHomeAgentBehaviour;
import FinalProject.BL.DataObjects.Problem;
import FinalProject.Service;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.mock;

public class ExperimentBuilderTest {

    private ExperimentBuilder builder;
    private Service service;

    @Before
    public void setUp() throws Exception
    {
        org.apache.log4j.BasicConfigurator.configure();
        service = mock(Service.class);
        builder = new ExperimentBuilder(service);

    }

    @After
    public void tearDown() throws Exception
    {
        builder = null;
    }

    @Test
    public void setNumOfIterationsGood() throws Exception
    {
        final int num = 1;
        builder.setNumOfIterations(num);
        Assert.assertEquals(num, builder.getNumOfIterations());
    }

    @Test
    public void addAlgorithmsGood() throws Exception
    {
        SmartHomeAgentBehaviour behaviour = mock(SmartHomeAgentBehaviour.class);
        final List<SmartHomeAgentBehaviour> lst = Collections.singletonList(behaviour);
        builder.addAlgorithms(lst);
        Assert.assertEquals(lst, builder.getAlgos());
    }

    @Test
    public void addProblemsGood() throws Exception
    {
        Problem prob = mock(Problem.class);
        final List<Problem> lst = Collections.singletonList(prob);
        builder.addProblems(lst);

        Assert.assertEquals(lst, builder.getProblems());
    }

    @Test
    public void createExperimentGood() throws Exception
    {
        final int num = 1;
        SmartHomeAgentBehaviour behaviour = mock(SmartHomeAgentBehaviour.class);
        final List<SmartHomeAgentBehaviour> algLst = Collections.singletonList(behaviour);
        builder.addAlgorithms(algLst);
        builder.setNumOfIterations(num);
        Problem prob = mock(Problem.class);
        final List<Problem> probLst = Collections.singletonList(prob);
        builder.addProblems(probLst);

        Experiment expected = new Experiment(service, probLst, algLst);
        expected.maximumIterations = num;
        Experiment res = builder.createExperiment();

        Assert.assertEquals(expected, res);
    }

    @Test(expected = RuntimeException.class)
    public void createExperimentAlgoNotSetBad() throws Exception
    {
        final int num = 1;
        builder.setNumOfIterations(num);
        Problem prob = mock(Problem.class);
        final List<Problem> probLst = Collections.singletonList(prob);
        builder.addProblems(probLst);

        builder.createExperiment(); //throws
    }

    @Test(expected = RuntimeException.class)
    public void createExperimentProblemNotSetBad() throws Exception
    {
        SmartHomeAgentBehaviour behaviour = mock(SmartHomeAgentBehaviour.class);
        final List<SmartHomeAgentBehaviour> algLst = Collections.singletonList(behaviour);
        builder.addAlgorithms(algLst);

        builder.createExperiment();
    }

    @Test(expected = RuntimeException.class)
    public void createExperimentNumIterNotSetBad() throws Exception
    {
        SmartHomeAgentBehaviour behaviour = mock(SmartHomeAgentBehaviour.class);
        final List<SmartHomeAgentBehaviour> algLst = Collections.singletonList(behaviour);
        builder.addAlgorithms(algLst);
        Problem prob = mock(Problem.class);
        final List<Problem> probLst = Collections.singletonList(prob);
        builder.addProblems(probLst);

        builder.createExperiment();
    }

}