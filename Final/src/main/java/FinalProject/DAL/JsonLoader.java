package FinalProject.DAL;

import FinalProject.BL.Problems.*;
import com.google.gson.*;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class JsonLoader implements JsonLoaderInterface {

    private final static Logger logger = Logger.getLogger(JsonLoader.class);
    private static Gson GSON = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
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
    public Map<Integer, List<Device>> loadDevices()
    {
        Map<Integer, List<Device>> homeTypeToDevices = new HashMap<>(3);
        String deviceDictPath = jsonsDir.getPath() + "\\DeviceDictionary" + FILE_TYPE;
        try(Reader reader = new BufferedReader(new FileReader(deviceDictPath)))
        {
            JsonParser parser = new JsonParser();
            JsonArray jsonArr = parser.parse(reader).getAsJsonArray();

            for (int i = 0; i < jsonArr.size(); i++)
            {
                List<Device> allDevices = new ArrayList<>();
                JsonObject deviceListObj = jsonArr.get(i).getAsJsonObject();
                deviceListObj.entrySet().forEach(entry -> {
                    Device curr;
                    JsonObject jsonObject = entry.getValue().getAsJsonObject();
                    if (jsonObject.get("type").getAsString().equals("sensor"))
                    {
                        curr = GSON.fromJson(jsonObject, Sensor.class);
                        curr.setName(entry.getKey());
                    }
                    else //actuator
                    {
                        curr = GSON.fromJson(jsonObject, Actuator.class);
                        curr.setName(entry.getKey());

                        JsonObject actObj = entry.getValue().getAsJsonObject().get("actions").getAsJsonObject();

                        List<Action> actions = new ArrayList<>();
                        for (Map.Entry<String, JsonElement> actEntry : actObj.entrySet())
                        {
                            Action act = GSON.fromJson(actEntry.getValue(), Action.class);
                            act.setName(actEntry.getKey());
                            actions.add(act);
                        }
                        ((Actuator)curr).setActions(actions);
                    }
                    allDevices.add(curr);
                });
                homeTypeToDevices.put(i, allDevices);
            }
        } catch (UnsupportedEncodingException e)
        {
            //TODO
            e.printStackTrace();
        } catch (IOException e)
        {
            //TODO
            e.printStackTrace();
        }
        return homeTypeToDevices;
    }

}
