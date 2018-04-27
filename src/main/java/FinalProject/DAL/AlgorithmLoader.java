package FinalProject.DAL;

import FinalProject.BL.Agents.SmartHomeAgentBehaviour;
import FinalProject.Config;
import com.vaadin.server.VaadinService;
import org.apache.log4j.Logger;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static org.jfree.util.ObjectUtilities.getClassLoader;

public class AlgorithmLoader implements AlgoLoaderInterface {

    private final static Logger logger = Logger.getLogger(AlgorithmLoader.class);
    private final static String UNCOMPILED_FILE_TYPE = ".java";
    private final static String COMPILED_FILE_TYPE = ".class";
    private final static String ADDED_ALGORITHMS_PATH = Config.getStringPropery(Config.ADDED_ALGORITHMS_DIR).replaceAll("/", Matcher.quoteReplacement(Matcher.quoteReplacement(File.separator)));

    private File addedAlgorithmsDir;
    private File compiledBaseDir;

    public AlgorithmLoader(String compiledFolderPath)
    {
        if (Files.exists(Paths.get(ADDED_ALGORITHMS_PATH)))
        {
            addedAlgorithmsDir = new File(ADDED_ALGORITHMS_PATH);
        }
        else
        {
            logger.warn(String.format("could not find the added algorithms path: %s", ADDED_ALGORITHMS_PATH));
        }
        if (Files.exists(Paths.get(compiledFolderPath)))
        {
            compiledBaseDir = new File(compiledFolderPath);
        }
        else
        {
            logger.warn(String.format("could not find the predefined algorithms path: %s", compiledFolderPath));
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
        Predicate<String> nameNotIgnored = name -> {
          if (ignoredNames.contains(name)) {
              return false;
          }
          for (String ignored : ignoredNames) {
              if (name.contains(ignored)) {
                  return false;
              }
          }
          return true;
        };
        ArrayList<File> allAlgorithms = new ArrayList<>();
        if (compiledBaseDir != null)
        {
            allAlgorithms.addAll(Arrays.asList(compiledBaseDir.listFiles()));
        }
        if (addedAlgorithmsDir != null)
        {
            allAlgorithms.addAll(Arrays.asList(addedAlgorithmsDir.listFiles()));
        }
        return allAlgorithms.stream()
                .map(File::getName)
                .filter(name -> name.endsWith(COMPILED_FILE_TYPE))
                .map(name -> name.substring(0, name.indexOf(COMPILED_FILE_TYPE)))
                .filter(nameNotIgnored)
                .collect(Collectors.toList());
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
        //TODO: to run with jetty: uncomment commented block and comment uncommented block
//        try
//        {
//            toReturn = SmartHomeAgentBehaviour.class.getClassLoader().loadClass("FinalProject.BL.Agents." + className);
//        }
//        catch (ClassNotFoundException | NoClassDefFoundError e)
//        {
//            logger.error("Failed Loading the Algorithm " + className, e);
//        }

//        ************************************************
//        if (className != null)
//        {
//            Path path = Paths.get(className);
//            try
//            {
//                URL[] urls = {path.toAbsolutePath().toUri().toURL()};
//                URLClassLoader loader = URLClassLoader.newInstance(urls);
//                toReturn = loader.loadClass(className);
//
//            } catch (MalformedURLException e)
//            {
//                logger.error("URL from path " + className + " is malformed", e);
//            } catch (ClassNotFoundException e)
//            {
//                logger.error("could not find class " + className + " in path " + className, e);
//            }
//        }
//        return toReturn;
//        ************************************************

//        try {
//            URL dirUrl = new File("resources/compiled_algorithms/FinalProject/BL/Agents/").getCanonicalFile().toURI().toURL();
//            URLClassLoader cl = new URLClassLoader(new URL[] {dirUrl}, VaadinService.getCurrent().getClassLoader());
//            Class loadedClass = cl.loadClass("FinalProject.BL.Agents." + className);
//            toReturn = loadedClass;
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return toReturn;
//        ************************************************
        try {

            String dirPathStr = Config.getStringPropery(Config.ADDED_ALGORITHMS_PACKAGE_DIR)
                    .replaceAll("/", Matcher.quoteReplacement(Matcher.quoteReplacement(File.separator)));
            URL dirUrl = new File(dirPathStr).toURI().toURL();
            URLClassLoader cl = (URLClassLoader) Thread.currentThread().getContextClassLoader();
            Method method = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{URL.class});
            method.setAccessible(true);
            method.invoke(cl, new Object[]{dirUrl});
            method.setAccessible(false);

            toReturn = cl.loadClass("FinalProject.BL.Agents." + className);
            //TODO clean caches
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoClassDefFoundError e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return toReturn;
    }

    private boolean compile(String pathStr) throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnosticsCollector = new DiagnosticCollector<>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnosticsCollector,  null, null);
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromStrings(Collections.singletonList(pathStr));
        String classPathStr = getClassPathStr();
        List<String> options = Arrays.asList("-d", Config.getStringPropery(Config.ADDED_ALGORITHMS_PACKAGE_DIR), "-classpath", classPathStr);
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnosticsCollector, options, null, compilationUnits);
        boolean success = task.call();
        fileManager.close();
        return success;
    }
    
    private String getClassPathStr() {
        URL[] urls = ((URLClassLoader) VaadinService.getCurrent().getClassLoader()).getURLs();
        String separator = System.getProperty("path.separator");
        StringBuilder builder = new StringBuilder();
        for (URL url : urls) {
            builder.append(url).append(separator);
        }
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }
}
