package FinalProjectTests;

import FinalProject.BL.Agents.DSA;
import FinalProject.BL.Agents.SmartHomeAgentBehaviour;
import FinalProject.BL.DataCollection.AlgorithmProblemResult;
import FinalProject.BL.DataObjects.Problem;
import FinalProject.DAL.DataAccessController;
import FinalProject.DAL.DataAccessControllerInterface;
import FinalProject.PL.UiHandlerInterface;
import FinalProject.Service;
import FinalProjectTests.DAL.DalTestUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

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
        service.setObserver(ui);
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
        service.setAlgorithmsToExperiment(new ArrayList<>(), numOfIter);
        Assert.assertEquals(service.getExperimentBuilder().getNumOfIterations(), numOfIter);
    }

    @Test
    public void addAlgorithmsToExperimentAlgoCorrectGood() throws Exception
    {
        when(accessController.getAlgorithms(new ArrayList<>()))
                .thenReturn(Collections.singletonList(behaviour));

        service.setAlgorithmsToExperiment(new ArrayList<>(), 1);
        Assert.assertEquals(service.getExperimentBuilder().getAlgos(),
                            Collections.singletonList(behaviour));
    }

    @Test
    public void addProblemsToExperimentGood() throws Exception
    {
        when(accessController.getProblems(new ArrayList<>()))
                .thenReturn(Collections.singletonList(problem));

        service.setProblemsToExperiment(new ArrayList<>());
        Assert.assertEquals(service.getExperimentBuilder().getProblems(),
                            Collections.singletonList(problem));
    }

    @Test
    public void stopExperiment() throws Exception
    {

    }

    @Test
    public void experimentEnded() throws Exception
    {
        List<AlgorithmProblemResult> someList = new ArrayList<>();
        Map<String, Long> probToAlgoTotalTime = new HashMap<>();
        service.experimentEnded(someList, probToAlgoTotalTime);
        verify(ui).notifyExperimentEnded(someList, probToAlgoTotalTime);
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