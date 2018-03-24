package FinalProject.DAL;

import FinalProject.BL.Agents.SmartHomeAgentBehaviour;
import org.apache.log4j.Logger;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public class AlgorithmLoader implements AlgoLoaderInterface {

    private final static Logger logger = Logger.getLogger(AlgorithmLoader.class);
    private final static String UNCOMPILED_FILE_TYPE = ".java";
    private final static String COMPILED_FILE_TYPE = ".class";
    private final static String DEFAULT_COMPILED_PATH = ""; //TODO
    private File compiledDir;

    public AlgorithmLoader(String compiledFolderPath)
    {
        if (Files.exists(Paths.get(compiledFolderPath)))
        {
            compiledDir = new File(compiledFolderPath);
        }
        else
        {
            compiledDir = new File(DEFAULT_COMPILED_PATH);
            logger.warn("cannot find given path. using default path instead");
        }
    }

    public List<SmartHomeAgentBehaviour> loadAlgorithms(List<String> algoNames)
    {
        if (algoNames != null)
        {
            return algoNames.parallelStream()
                    .distinct()
                    .map(name -> loadClassFromFile(name))
                    .filter(this::verifyClassIsAlgorithm)
                    .map(cls ->
                         {
                             try
                             {
                                 return (SmartHomeAgentBehaviour) cls.newInstance();
                             } catch (InstantiationException | IllegalAccessException e)
                             {
                                 logger.warn("Could not instantiate class " + cls.getSimpleName(), e);
                                 return null;
                             }
                         })
                    .filter(obj -> obj != null)
                    .collect(Collectors.toList());
        }
        logger.warn("loadAlgorithms got NULL as argument. Returning empty list.");
        return new ArrayList<>(0);
    }

    public List<String> getAllAlgoNames()
    {
        if (compiledDir == null)
        {
            logger.error("compiledDir is null!");
            return null;
        }
        List<File> allInFolder = Arrays.asList(compiledDir.listFiles());
        return allInFolder.stream()
                .map(File::getName)
                .filter(name -> name.endsWith(COMPILED_FILE_TYPE))
                .map(name -> name.substring(0, name.indexOf(COMPILED_FILE_TYPE)))
                .collect(Collectors.toList());
    }

    public void addAlgoToSystem(String path, String fileName)
            throws IOException, InstantiationException, IllegalAccessException
    {
        if (fileName.endsWith(COMPILED_FILE_TYPE))
        {
            throw new IOException("Could not compile a .class file!");
        }
        if (fileName.endsWith(UNCOMPILED_FILE_TYPE))
        {
            fileName = fileName.substring(0, fileName.indexOf(UNCOMPILED_FILE_TYPE));
        }

        boolean compilationSuccess = compile(path + Matcher.quoteReplacement(File.separator) + fileName + UNCOMPILED_FILE_TYPE);
        if (!compilationSuccess)
        {
            throw new IOException("Could not compile class " + fileName);
        }

        if (!verifyClassIsAlgorithm(fileName))
        {
            File file = new File(compiledDir.getPath() + Matcher.quoteReplacement(File.separator) + fileName + COMPILED_FILE_TYPE);

            if(!file.delete())
            {
                System.err.println("could not delete file " + fileName);
            }
        }
    }

    private boolean verifyClassIsAlgorithm(Class compiledClass)
    {
        if (compiledClass != null)
        {
            try
            {
                return  compiledClass.newInstance() instanceof SmartHomeAgentBehaviour;
            } catch (InstantiationException e)
            {
                logger.error("Could not instantiate class " + compiledClass.getSimpleName(), e);
                return false;
            } catch (IllegalAccessException e)
            {
                logger.error("Could not access class " + compiledClass.getSimpleName(), e);
                return false;
            }
        }
        logger.warn("verifyClassIsAlgorithm got NULL as argument");
        return false;
    }

    private boolean verifyClassIsAlgorithm(String fileName)
    {
        Class compiledClass = loadClassFromFile(fileName);
        return  verifyClassIsAlgorithm(compiledClass);

    }

    /**
     * load class from compiled .class file
     * @param className
     * @return
     * @throws ClassNotFoundException
     */
    private Class loadClassFromFile(String className)
    {
        Class toReturn = null;
//        try
//        {
//            toReturn = SmartHomeAgentBehaviour.class.getClassLoader().loadClass("FinalProject.BL.Agents." + className);
//        }
//        catch (ClassNotFoundException | NoClassDefFoundError e)
//        {
//            logger.error("Failed Loading the Algorithm " + className, e);
//        }
        if (className != null)
        {
            Path path = Paths.get(className);
            try
            {
                URL[] urls = {path.toAbsolutePath().toUri().toURL()};
                URLClassLoader loader = URLClassLoader.newInstance(urls);
                toReturn = loader.loadClass(className);

            } catch (MalformedURLException e)
            {
                logger.error("URL from path " + className + " is malformed", e);
            } catch (ClassNotFoundException e)
            {
                logger.error("could not find class " + className + " in path " + className, e);
            }
        }
        return toReturn;
    }

    private boolean compile(String pathStr) throws IOException
    {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        //DiagnosticCollector<JavaFileObject> diagnosticsCollector = new DiagnosticCollector<>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null/*diagnosticsCollector*/, null, null);
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromStrings(Collections.singletonList(pathStr));
        List<String> options = Arrays.asList("-d", compiledDir.getPath());
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, null/*diagnosticsCollector*/, options, null, compilationUnits);
        boolean success = task.call();
        fileManager.close();
        return success;
    }
}
