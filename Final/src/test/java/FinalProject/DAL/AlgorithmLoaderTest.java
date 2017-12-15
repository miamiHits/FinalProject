package FinalProject.DAL;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AlgorithmLoaderTest {

    private AlgorithmLoader loader;
    private final static String compiledDirPath = "src\\test\\testResources\\compiledAlgorithms";
    private final static String uncompiledDirPath = "src\\test\\testResources\\uncompiledAlgorithms";
    private List<String> classesToDelete;

    @Before
    public void setUp() throws Exception
    {
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