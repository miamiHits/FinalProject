package FinalProject.DAL;

import FinalProject.BL.Agents.SmartHomeAgentBehaviour;
import com.vaadin.server.VaadinService;
import org.apache.log4j.Logger;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public class AlgorithmLoader implements AlgoLoaderInterface {

    private final static Logger logger = Logger.getLogger(AlgorithmLoader.class);
    private final static String UNCOMPILED_FILE_TYPE = ".java";
    private final static String COMPILED_FILE_TYPE = ".class";
    private final static String DEFAULT_COMPILED_PATH = "resources/compiled_algorithms";
    private final static String COMPILED_PACKAGE_NAME = "/FinalProject/BL/Agents/";
    private File compiledBaseDir;

    public AlgorithmLoader(String compiledFolderPath)
    {
        if (Files.exists(Paths.get(compiledFolderPath)))
        {
            compiledBaseDir = new File(compiledFolderPath);
        }
        else
        {
            compiledBaseDir = new File(DEFAULT_COMPILED_PATH);
            logger.warn("cannot find given path. using default path instead");
        }
    }

    public List<SmartHomeAgentBehaviour> loadAlgorithms(List<String> algoNames)
    {
        if (algoNames != null) {
            return algoNames.parallelStream()
                    .distinct()
                    .map(this::loadClassFromFile)
                    .filter(this::verifyClassIsAlgorithm)
                    .map(cls -> {
                             try {
                                 return (SmartHomeAgentBehaviour) cls.newInstance();
                             } catch (InstantiationException | IllegalAccessException e)
                             {
                                 logger.warn("Could not instantiate class " + cls.getSimpleName(), e);
                                 return null;
                             }
                         })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
        logger.warn("loadAlgorithms got NULL as argument. Returning empty list.");
        return new ArrayList<>(0);
    }

    public List<String> getAllAlgoNames()
    {
        if (compiledBaseDir == null) {
            logger.error("compiledPackagedDir is null!");
            return null;
        }
        Set<String> ignoredNames = new HashSet<>(Arrays.asList("SmartHomeAgentBehaviour", "AlgorithmDataHelper",
                "PropertyWithData", "ImprovementMsg", "SmartHomeAgent"));
        String fullPathWithPkg = compiledBaseDir.getPath() + COMPILED_PACKAGE_NAME;
        try {
            return Files.walk(compiledBaseDir.toPath(), FileVisitOption.FOLLOW_LINKS)
                    .map(Path::toString)
                    .filter(file -> file.endsWith(COMPILED_FILE_TYPE) && !file.contains("$"))
                    //leave only file name
                    .map(withFileType -> withFileType.substring(fullPathWithPkg.length(),
                            withFileType.length() - COMPILED_FILE_TYPE.length()))
                    .filter(file -> !ignoredNames.contains(file))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            logger.error("exception while trying to get all allgo names!");
            return null;
        }
    }

    public String addAlgoToSystem(String path, String fileName) {
        if (fileName.endsWith(COMPILED_FILE_TYPE)) {
            return "Only .java files are valid algorithm files!";
        }
        if (fileName.endsWith(UNCOMPILED_FILE_TYPE)) {
            fileName = fileName.substring(0, fileName.indexOf(UNCOMPILED_FILE_TYPE));
        }

        boolean compilationSuccess;
        try {
            compilationSuccess = compile(path + Matcher.quoteReplacement(File.separator) + fileName + UNCOMPILED_FILE_TYPE);
        } catch (IOException e) {
            return "Could not compile file " + fileName + ", an exception accrued!";
        }
        if (!compilationSuccess) {
            return "Could not compile file " + fileName;
        }

        if (!verifyClassIsAlgorithm(fileName)) {
            File file = new File(compiledBaseDir.getPath() + Matcher.quoteReplacement(File.separator) + fileName + COMPILED_FILE_TYPE);
            if(!file.delete()) {
                System.err.println("could not delete file " + fileName);
            }
            return "File " + fileName +" is not a valid algorithm file!";
        }
        deleteJavaFile(fileName);
        return "Success";
    }

    private void deleteJavaFile(String fileName) {
        if (fileName.endsWith(COMPILED_FILE_TYPE)) {
            return;
        }
        if (fileName.endsWith(UNCOMPILED_FILE_TYPE)) {
            fileName = fileName.substring(0, fileName.indexOf(UNCOMPILED_FILE_TYPE));
        }

        String pathStr = compiledBaseDir.getPath() + "/" + fileName + ".java"
                .replaceAll("/", Matcher.quoteReplacement(Matcher.quoteReplacement(File.separator)));
        File file = new File(pathStr);
        if (!file.exists() || !file.delete()) {
            logger.warn("could not delete java file " + pathStr);
        }
    }

    private boolean verifyClassIsAlgorithm(Class compiledClass) {
        if (compiledClass != null) {
            try
            {
                return compiledClass.newInstance() instanceof SmartHomeAgentBehaviour;
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
        return verifyClassIsAlgorithm(compiledClass);

    }

    /**
     * load class from compiled .class file
     * @param className
     * @return
     * @throws ClassNotFoundException
     */
    private Class loadClassFromFile(String className) {
        Class toReturn = null;
        try {

            String dirPathStr = "target/classes/FinalProject/BL/Agents/"
                    .replaceAll("/", Matcher.quoteReplacement(Matcher.quoteReplacement(File.separator)));
            URL dirUrl = new File(dirPathStr).toURI().toURL();
            URLClassLoader cl = (URLClassLoader) Thread.currentThread().getContextClassLoader();
            Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            method.invoke(cl, dirUrl);
            method.setAccessible(false);

            toReturn = cl.loadClass("FinalProject.BL.Agents." + className);
        } catch (IOException | NoSuchMethodException | InvocationTargetException |
                IllegalAccessException | ClassNotFoundException | NoClassDefFoundError e) {
            logger.warn("Exception while loading file " + className);
        }
        return toReturn;
    }

    private boolean compile(String pathStr) throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnosticsCollector = new DiagnosticCollector<>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnosticsCollector,  null, null);
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromStrings(Collections.singletonList(pathStr));
        String classPathStr = getClassPathStr();
        List<String> options = Arrays.asList("-d", compiledBaseDir.getPath(), "-classpath", classPathStr);
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnosticsCollector, options, null, compilationUnits);
        boolean success = task.call();
        fileManager.close();
        return success;
    }
    
    private String getClassPathStr() {
        URL[] urls = getUrlClassLoader().getURLs();
        String separator = System.getProperty("path.separator");
        StringBuilder builder = new StringBuilder();
        for (URL url : urls) {
            builder.append(url).append(separator);
        }
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }

    private URLClassLoader getUrlClassLoader() {
        VaadinService vaadinService =  VaadinService.getCurrent();
        //if vaadin is running
        if (vaadinService != null) {
            return (URLClassLoader) vaadinService.getClassLoader();
        }
        //if vaadin is NOT running (tests)
        else {
            return (URLClassLoader) Thread.currentThread().getContextClassLoader();
        }
    }
}
