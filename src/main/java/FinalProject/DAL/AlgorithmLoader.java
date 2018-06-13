package FinalProject.DAL;

import FinalProject.BL.Agents.SmartHomeAgentBehaviour;
import FinalProject.BL.AlgoAddResult;
import FinalProject.Config;
import com.vaadin.server.VaadinService;
import org.apache.log4j.Logger;

import javax.tools.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
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

import static org.jfree.util.ObjectUtilities.getClassLoader;

public class AlgorithmLoader implements AlgoLoaderInterface {

    private final static Logger logger = Logger.getLogger(AlgorithmLoader.class);
    private final static String UNCOMPILED_FILE_TYPE = ".java";
    private final static String COMPILED_FILE_TYPE = ".class";
    private final static String ADDED_ALGORITHMS_PATH = Config.getStringPropery(Config.ADDED_ALGORITHMS_PACKAGE_DIR).replaceAll("/", Matcher.quoteReplacement(Matcher.quoteReplacement(File.separator)));
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
        String fullPathWithPkg = compiledBaseDir.getPath() + "/";
        try {
            List<String> allAlgorithms = Files.walk(compiledBaseDir.toPath(), FileVisitOption.FOLLOW_LINKS)
                    .map(Path::toString)
                    .filter(file -> file.endsWith(COMPILED_FILE_TYPE) && !file.contains("$"))
                    //leave only file name
                    .map(withFileType -> withFileType.substring(fullPathWithPkg.length(),
                            withFileType.length() - COMPILED_FILE_TYPE.length()))
                    .filter(file -> !ignoredNames.contains(file))
                    .collect(Collectors.toList());
            if (addedAlgorithmsDir != null)
            {
                List<String> addedAlgorithms = Arrays.asList(addedAlgorithmsDir.listFiles()).stream()
                        .map(File::getName)
                        .filter(name -> name.endsWith(COMPILED_FILE_TYPE))
                        .map(name -> name.substring(0, name.indexOf(COMPILED_FILE_TYPE)))
                        .collect(Collectors.toList());

                allAlgorithms.addAll(addedAlgorithms);
            }
            return allAlgorithms;

        } catch (IOException e) {
            logger.error("exception while trying to get all allgo names!");
            return new ArrayList<>();
        }
    }

    public AlgoAddResult addAlgoToSystem(String path, String fileName) {
        if (fileName.endsWith(COMPILED_FILE_TYPE)) {
            new AlgoAddResult(false, "Only .java files are valid algorithm files!");
        }
        if (fileName.endsWith(UNCOMPILED_FILE_TYPE)) {
            fileName = fileName.substring(0, fileName.indexOf(UNCOMPILED_FILE_TYPE));
        }

        AlgoAddResult compiRes;
        try {
            compiRes = compile(path + fileName + UNCOMPILED_FILE_TYPE);
        } catch (IOException | InterruptedException e) {
            return new AlgoAddResult(false, e.getMessage());
        }
        if (!compiRes.isSuccess()) {
            return compiRes;
        }

        if (!verifyClassIsAlgorithm(fileName)) {
            File file = new File(compiledBaseDir.getPath() + Matcher.quoteReplacement(File.separator) + fileName + COMPILED_FILE_TYPE);
            if(!file.delete()) {
                System.err.println("could not delete file " + fileName);
            }
            return new AlgoAddResult(false, "File " + fileName +" is not a valid algorithm file!");
        }
        deleteJavaFile(fileName);
        return compiRes;
    }

    private void deleteJavaFile(String fileName) {
        if (fileName.endsWith(COMPILED_FILE_TYPE)) {
            return;
        }
        if (fileName.endsWith(UNCOMPILED_FILE_TYPE)) {
            fileName = fileName.substring(0, fileName.indexOf(UNCOMPILED_FILE_TYPE));
        }

        String pathStr = Paths.get(Config.getStringPropery(Config.ADDED_ALGORITHMS_PACKAGE_DIR), fileName + ".java").toAbsolutePath().toString();
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

            String dirPathStr = Config.getStringPropery(Config.ADDED_ALGORITHMS_DIR)
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

    private AlgoAddResult compile(String pathStr) throws IOException, InterruptedException {
        String javaFilePath = Paths.get(pathStr).toAbsolutePath().toString();
        String classpath = getClassPathStr();
        String currentOS = System.getProperty("os.name").toLowerCase();
        if (currentOS.contains("windows"))
        {
            classpath = classpath.replaceAll("file:/", "");
        }
        String javacPath = Config.getStringPropery(Config.JAVAC_PATH);
        String command = javacPath + " " + javaFilePath + " -cp " + classpath;
        Process pro = Runtime.getRuntime().exec(command);
        String line = null;
        StringBuilder javacOutput = new StringBuilder();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(pro.getErrorStream()));
        while ((line = in.readLine()) != null) {
            logger.info("javac output line: " + line);
            javacOutput.append(line);
        }
        boolean success = false;
        String errorMessage = "";
        int exitCode = pro.waitFor();
        if (exitCode == 0)
        {
            success = true;
        }
        else
        {
            errorMessage = "compilation failed with the following output(available in the log file):\n" +
                    javacOutput.toString();
            logger.warn(errorMessage);
        }
        return new AlgoAddResult(success, errorMessage);
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
