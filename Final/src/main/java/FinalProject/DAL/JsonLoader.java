package FinalProject.DAL;

import FinalProject.BL.Problems.Device;
import FinalProject.BL.Problems.Problem;
import com.google.gson.Gson;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class JsonLoader implements JsonLoaderInterface {

    private final static Logger logger = Logger.getLogger(JsonLoader.class);
    private final static Gson GSON = new Gson();
    private final static String DEVICE_DICT_FILE_NAME = "DeviceDictionary";
    private final static String FILE_TYPE = ".json";
    private final static String DEFAULT_PATH = ""; //TODO
    private File jsonsDir;

    public JsonLoader(String path)
    {
        if (Files.exists(Paths.get(path)))
        {
            jsonsDir = new File(path);
        }
        else
        {
            jsonsDir = new File(DEFAULT_PATH);
            logger.warn("cannot find given path. using default path instead");
        }
    }

    public File getJsonsDir()
    {
        return jsonsDir;
    }

    @Override
    public List<Problem> loadProblems(List<String> problemNames)
    {
        return null;
    }

    private Problem loadSingleProblem(String problemName)
    {
        Problem problem = null;
        try(Reader reader = new InputStreamReader(JsonLoader.class.getResourceAsStream(jsonsDir.getName() + FILE_TYPE), "UTF-8"))
        {
            problem = GSON.fromJson(reader, Problem.class);
            System.out.println(problem);
        } catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return problem;
    }

    @Override
    public List<String> getAllProblemNames()
    {
        List<File> allInFolder = Arrays.asList(jsonsDir.listFiles());
        return allInFolder.stream()
                .map(File::getName)
                .filter(name -> name.endsWith(FILE_TYPE) && !name.equals("DeviceDictionary" + FILE_TYPE))
                .map(name -> name.substring(0, name.indexOf(FILE_TYPE)))
                .collect(Collectors.toList());
    }

    @Override
    public List<Device> getAllDevices()
    {
        return null;
    }
}
