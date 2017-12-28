package FinalProject;

import FinalProject.DAL.DataAccessController;
import FinalProject.DAL.DataAccessControllerInterface;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;

public class ServiceTest {

    private Service service;
    private DataAccessControllerInterface accessController;

    @Before
    public void setUp() throws Exception
    {
        accessController = mock(DataAccessController.class);

        service = new Service(accessController);
    }

    @After
    public void tearDown() throws Exception
    {

    }

    @Test
    public void addAlgorithmsToExperiment() throws Exception
    {

    }

    @Test
    public void addProblemsToExperiment() throws Exception
    {

    }

    @Test
    public void runExperiment() throws Exception
    {

    }

    @Test
    public void stopExperiment() throws Exception
    {

    }

    @Test
    public void getExperimentResults() throws Exception
    {

    }

    @Test
    public void experimentEnded() throws Exception
    {

    }

    @Test
    public void experimentEndedWithError() throws Exception
    {

    }

    @Test
    public void saveExperimentResult() throws Exception
    {

    }

}