package FinalProject.DAL;

import FinalProject.BL.Agents.SmartHomeAgentBehaviour;
import FinalProject.Config;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;

import org.mockito.internal.util.collections.Sets;

public class AlgorithmLoaderTest {

    private AlgorithmLoader loader;
    private List<String> classesToDelete;

    @Before
    public void setUp() {
        org.apache.log4j.BasicConfigurator.configure();
        Config.loadTestConfig();
        loader = new AlgorithmLoader(DalTestUtils.compiledDirBasePath);
        classesToDelete = new ArrayList<>();
    }

    @After
    public void tearDown() {
        loader = null;

        classesToDelete.forEach(className -> {
            File file = new File(DalTestUtils.compiledDirBasePath + Matcher.quoteReplacement(File.separator) + className + ".class");
            if(!file.delete()) {
                System.err.println("could not delete file " + className);
            }
        });

        classesToDelete = null;
    }

    @Test
    public void loadAlgorithmsGood() {
        loader.addAlgoToSystem(DalTestUtils.uncompiledDirPath, "BehaviourToCompile");

        List<String> toLoad = Collections.singletonList("BehaviourToCompile");
        List<SmartHomeAgentBehaviour> actual = loader.loadAlgorithms(toLoad);

        Assert.assertEquals(toLoad.size(), actual.size());

        classesToDelete.addAll(toLoad);
    }

    @Test
    public void loadAlgorithmsNullAndAlgoInListBad() {
        loader.addAlgoToSystem(DalTestUtils.uncompiledDirPath, "BehaviourToCompile");
        classesToDelete.add("BehaviourToCompile");

        List<String> toLoad = new ArrayList<>(2);
        toLoad.add(null);
        toLoad.add("BehaviourToCompile");
        List<SmartHomeAgentBehaviour> actual = loader.loadAlgorithms(toLoad);

        Assert.assertEquals(1, actual.size());

    }

    @Test
    public void loadAlgorithmsNotAlgoBad() {
        List<String> toLoad = Collections.singletonList("App");
        List<SmartHomeAgentBehaviour> actual = loader.loadAlgorithms(toLoad);

        Assert.assertTrue(actual.isEmpty());
    }

    @Test
    public void loadAlgorithmsNotAllAreAlgoBad() throws Exception
    {
        loader.addAlgoToSystem(DalTestUtils.uncompiledDirPath, "BehaviourToCompile");

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
        Set<String> expected = Sets.newSet("SHMGM", "DSA", "SA");
        List<String> actual = loader.getAllAlgoNames();
        Assert.assertEquals(expected, new HashSet<>(actual));
    }

    @Test
    public void addAlgoToSystemGood() {
        String fileName = "BehaviourToCompile";

        loader.addAlgoToSystem(DalTestUtils.uncompiledDirPath, fileName + ".java");
        classesToDelete.add(fileName);

        File classFile = new File(Config.getStringPropery(Config.ADDED_ALGORITHMS_PACKAGE_DIR));
        Assert.assertTrue(classFile.exists());
    }

    @Test
    public void addAlgoToSystemBadNotImplInterface() {
        String fileName = "BehaviourToCompileNotImpl";

        loader.addAlgoToSystem(DalTestUtils.uncompiledDirPath, fileName + ".java");
        classesToDelete.add(fileName);

        File classFile = new File(Config.getStringPropery(Config.ADDED_ALGORITHMS_PACKAGE_DIR) + fileName + ".class");
        Assert.assertFalse(classFile.exists());
    }

    @Test
    public void addAlgoToSystemNotBehaviourBad() {
        String fileName = "SomeClassToCompile";
        classesToDelete.add(fileName);
        loader.addAlgoToSystem(DalTestUtils.uncompiledDirPath, fileName + ".java");

        File classFile = new File(DalTestUtils.packagePath + Matcher.quoteReplacement(File.separator) + fileName + ".class");
        Assert.assertFalse(classFile.exists());
    }

}