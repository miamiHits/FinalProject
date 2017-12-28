package FinalProject;

import FinalProject.BL.Agents.DSA;
import FinalProject.BL.Agents.SmartHomeAgentBehaviour;
import FinalProject.BL.DataCollection.AlgorithmProblemResult;
import FinalProject.BL.Problems.Problem;
import FinalProject.DAL.DalTestUtils;
import FinalProject.DAL.DataAccessController;
import FinalProject.DAL.DataAccessControllerInterface;
import FinalProject.PL.UiHandlerInterface;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ServiceTest {

    private Service service;
    private DataAccessControllerInterface accessController;
    private SmartHomeAgentBehaviour behaviour;
    private Problem problem;
    private UiHandlerInterface ui;

    @Before
    public void setUp() throws Exception
    {
        org.apache.log4j.BasicConfigurator.configure();

        behaviour = new DSA();
        problem = DalTestUtils.getProblemDm_7_1_2();

        ui = mock(UiHandlerInterface.class);
        accessController = mock(DataAccessController.class);

        service = new Service(accessController);
        service.addObserver(ui);
    }

    @After
    public void tearDown() throws Exception
    {
        service = null;
        accessController = null;
        behaviour = null;
        problem = null;
    }

    @Test
    public void addAlgorithmsToExperimentNumOfIterCorrectGood() throws Exception
    {
        when(accessController.getAlgorithms(new ArrayList<>()))
                .thenReturn(Collections.singletonList(behaviour));

        int numOfIter = 1;
        service.addAlgorithmsToExperiment(new ArrayList<>(), numOfIter);
        Assert.assertEquals(service.getExperimentBuilder().getNumOfIterations(), numOfIter);
    }

    @Test
    public void addAlgorithmsToExperimentAlgoCorrectGood() throws Exception
    {
        when(accessController.getAlgorithms(new ArrayList<>()))
                .thenReturn(Collections.singletonList(behaviour));

        service.addAlgorithmsToExperiment(new ArrayList<>(), 1);
        Assert.assertEquals(service.getExperimentBuilder().getAlgos(),
                            Collections.singletonList(behaviour));
    }

    @Test
    public void addProblemsToExperimentGood() throws Exception
    {
        when(accessController.getProblems(new ArrayList<>()))
                .thenReturn(Collections.singletonList(problem));

        service.addProblemsToExperiment(new ArrayList<>());
        Assert.assertEquals(service.getExperimentBuilder().getProblems(),
                            Collections.singletonList(problem));
    }

    @Test
    public void runExperiment() throws Exception
    {
        service.runExperiment();
        Assert.assertNotNull(service.currExperiment);
    }

    @Test
    public void stopExperiment() throws Exception
    {

    }

    @Test
    public void experimentEnded() throws Exception
    {
        List<AlgorithmProblemResult> someList = new ArrayList<>();
        service.experimentEnded(someList);
        verify(ui).update(service, someList);
    }

    @Test
    public void experimentEndedWithError() throws Exception
    {
        //TODO
    }

    @Test
    public void saveExperimentResult() throws Exception
    {
        //TODO
        //not implemented yet
    }

}