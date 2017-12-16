package FinalProject.DAL;

import FinalProject.BL.Agents.SmartHomeAgentBehaviour;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AlgorithmLoaderTest {

    private AlgorithmLoader loader;
    private final static String compiledDirPath = "src\\test\\testResources\\compiledAlgorithms";
    private final static String uncompiledDirPath = "src\\test\\testResources\\uncompiledAlgorithms";
    private List<String> classesToDelete;

    @Before
    public void setUp() throws Exception
    {
        org.apache.log4j.BasicConfigurator.configure();
        loader = new AlgorithmLoader(compiledDirPath);
        classesToDelete = new ArrayList<>();
    }

    @After
    public void tearDown() throws Exception
    {
        loader = null;

        classesToDelete.forEach(className -> {
            File file = new File(compiledDirPath + "\\" + className + ".class");

            if(!file.delete())
            {
                System.err.println("could not delete file " + className);
            }
        });

        classesToDelete = null;
    }

//    @Test
//    public void compileTest() throws Exception
//    {
//        String fileName = "SomeClassToCompile";
//        String pathToCompile = uncompiledDirPath + "\\" + fileName + ".java";
////        classesToDelete.add(fileName);
//        loader.compile(pathToCompile);
//    }

    @Test
    public void loadAlgorithmsGood() throws Exception
    {
        loader.addAlgoToSystem(uncompiledDirPath, "BehaviourToCompile");

        List<String> toLoad = Arrays.asList("BehaviourToCompile");
        List<SmartHomeAgentBehaviour> actual = loader.loadAlgorithms(toLoad);

        Assert.assertEquals(toLoad.size(), actual.size());

        classesToDelete.addAll(toLoad);
    }

    @Test
    public void loadAlgorithmsNullAndAlgoInListBad() throws Exception
    {
        loader.addAlgoToSystem(uncompiledDirPath, "BehaviourToCompile");

        List<String> toLoad = new ArrayList<>(2);
        toLoad.add(null);
        toLoad.add("BehaviourToCompile");
        List<SmartHomeAgentBehaviour> actual = loader.loadAlgorithms(toLoad);

        Assert.assertEquals(1, actual.size());

        classesToDelete.add("BehaviourToCompile");
    }

    @Test
    public void loadAlgorithmsNotAlgoBad() throws Exception
    {
        List<String> toLoad = Arrays.asList("App");
        List<SmartHomeAgentBehaviour> actual = loader.loadAlgorithms(toLoad);

        Assert.assertTrue(actual.isEmpty());
    }

    @Test
    public void loadAlgorithmsNotAllAreAlgoBad() throws Exception
    {
        loader.addAlgoToSystem(uncompiledDirPath, "BehaviourToCompile");

        List<String> toLoad = Arrays.asList("App", "BehaviourToCompile");
        List<SmartHomeAgentBehaviour> actual = loader.loadAlgorithms(toLoad);

        Assert.assertEquals(1, actual.size());

        classesToDelete.add("BehaviourToCompile");
    }

    @Test
    public void loadAlgorithmsNullNameAlgoBad() throws Exception
    {
        List<String> toLoad = new ArrayList<>(1);
        toLoad.add(null);
        List<SmartHomeAgentBehaviour> actual = loader.loadAlgorithms(toLoad);

        Assert.assertTrue(actual.isEmpty());
    }

    @Test
    public void loadAlgorithmsNullListAlgoBad() throws Exception
    {
        List<SmartHomeAgentBehaviour> actual = loader.loadAlgorithms(null);

        Assert.assertTrue(actual.isEmpty());
    }

    @Test
    public void getAllAlgoNames() throws Exception
    {
        List<String> expected = Arrays.asList("App");
        List<String> actual = loader.getAllAlgoNames();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void addAlgoToSystemGood() throws Exception
    {
        String fileName = "BehaviourToCompile";
        loader.addAlgoToSystem(uncompiledDirPath, fileName + ".java");

        File classFile = new File(compiledDirPath + "\\" + fileName + ".class");
        Assert.assertTrue(classFile.exists());

        classesToDelete.add(fileName);
    }

    @Test
    public void addAlgoToSystemNotBehaviourBad() throws Exception
    {
        String fileName = "SomeClassToCompile";
        loader.addAlgoToSystem(uncompiledDirPath, fileName + ".java");

        File classFile = new File(compiledDirPath + "\\" + fileName + ".class");
        Assert.assertFalse(classFile.exists());
    }

}