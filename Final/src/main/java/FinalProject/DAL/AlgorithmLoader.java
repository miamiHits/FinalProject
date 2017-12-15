package FinalProject.DAL;

import FinalProject.BL.Agents.SmartHomeAgentBehaviour;
import com.sun.org.apache.xalan.internal.xsltc.compiler.CompilerException;
import jade.core.behaviours.Behaviour;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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

    public List<Behaviour> loadAlgorithms(List<String> algoNames)
    {
        return null;
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

        boolean compilationSuccess = compile(path + "\\" + fileName + UNCOMPILED_FILE_TYPE);
        if (!compilationSuccess)
        {
            throw new IOException("Could not compile class " + fileName);
        }

        if (!verifyClassIsAlgorithm(fileName))
        {
            File file = new File(compiledDir.getPath() + "\\" + fileName + COMPILED_FILE_TYPE);

            if(!file.delete())
            {
                System.err.println("could not delete file " + fileName);
            }
        }
    }

    private boolean verifyClassIsAlgorithm(String fileName)
            throws IllegalAccessException, InstantiationException
    {
        try
        {
            Class compiledClass = loadClassFromFile(compiledDir.getPath(), fileName);
            return  compiledClass.newInstance() instanceof SmartHomeAgentBehaviour;

        } catch (ClassNotFoundException e)
        {
            logger.warn("Could not find compiled class " + fileName + COMPILED_FILE_TYPE);
            return false;
        }
    }

    /**
     * load class from compiled .class file
     * @param pathStr
     * @param className
     * @return
     * @throws ClassNotFoundException
     */
    private static Class loadClassFromFile(String pathStr, String className) throws ClassNotFoundException
    {
        Path path = Paths.get(pathStr);
        Class toReturn = null;
        try
        {
            URL[] urls = {path.toAbsolutePath().toUri().toURL()};
            URLClassLoader loader = URLClassLoader.newInstance(urls);
            toReturn = loader.loadClass(className);

        } catch (MalformedURLException e)
        {
            //TODO: this
            e.printStackTrace();
        }
        return toReturn;
    }

    //TODO: make this private
    public boolean compile(String pathStr) throws IOException
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

    //*************************************************************************

    /**
     * Get a Class object by compiling and loading a '.java' file.
     * the '.class' file created by compilation is deleted when the JVM terminates.
     * @param pathStr a relative to absolute path to the '.java' file.
     *             example: "some\dir\path"
     * @param className name of the file to load, including or not including
     *                  the '.java' suffix;
     * @return Class object representing the class of the file {@param className}
     * @throws CompilerException if the file to load cannot be compiled by
     * the java compiler
     * @throws ClassNotFoundException if the file is not a '.java' file.
     */
    public Class loadClassFromJavaFile(String pathStr, String className)
            throws CompilerException, ClassNotFoundException
    {
        String fileName;
        Class toReturn = null;

        if (className.endsWith(".java"))
        {
            fileName = className;
            className = className.substring(0, className.length() - 5);
        }
        else if (className.contains("."))
        {
            throw new ClassNotFoundException("the file is not a '.java' file");
        }
        else
        {
            fileName = className + ".java";
        }
        try
        {
            boolean compilationSuccess = compile(pathStr + "\\" + fileName);
            if (compilationSuccess)
            {
                toReturn = loadClassFromFile(pathStr, className);
                deleteClassFileOnExit(pathStr, className + ".class");
            }
            else
            {
                throw new CompilerException("Unable to compile the file " + fileName);
            }
        }
        catch (IOException ignored) { }

        return toReturn;
    }

    private static void deleteClassFileOnExit(String pathStr, String fileName)
    {
        File classFile = new File(pathStr + "\\" + fileName);
        classFile.deleteOnExit();
    }

}
