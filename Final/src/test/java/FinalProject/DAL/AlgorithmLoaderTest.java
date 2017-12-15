package FinalProject.DAL;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AlgorithmLoaderTest {

    private AlgoLoaderInterface loader;
    private final static String compiledDirPath = "src\\test\\testResources\\compiledAlgorithms";

    @Before
    public void setUp() throws Exception
    {
        loader = new AlgorithmLoader(compiledDirPath);
    }

    @After
    public void tearDown() throws Exception
    {
        loader = null;
    }

    @Test
    public void loadAlgorithms() throws Exception
    {

    }

    @Test
    public void getAllAlgoNames() throws Exception
    {
        //TODO
    }

    @Test
    public void addAlgoToSystem() throws Exception
    {

    }

}