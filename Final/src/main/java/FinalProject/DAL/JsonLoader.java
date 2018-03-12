package FinalProject.DAL;

import FinalProject.BL.DataObjects.*;
import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class JsonLoader implements JsonLoaderInterface {

    private final static Logger logger = Logger.getLogger(JsonLoader.class);
    private static Gson gson;
    private final static String DEVICE_DICT_FILE_NAME = "DeviceDictionary";
    private final static String FILE_TYPE = ".json";
    private final static String DEFAULT_PATH = ""; //TODO
    private File jsonsDir;
    private static Map<Integer, List<Device>> deviceDict;

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
        GsonBuilder builder = new GsonBuilder();
        builder.setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE);
        gson = builder.create();
    }

    public Map<Integer, List<Device>> getDeviceDict()
    {
        if (deviceDict == null)
        {
            loadDevices();
        }
        return deviceDict;
    }

    public File getJsonsDir()
    {
        return jsonsDir;
    }

    public void setJsonsDir(File jsonsDir)
    {
        this.jsonsDir = jsonsDir;
    }

    @Override
    public List<Problem> loadProblems(List<String> problemNames)
    {
        if (problemNames != null)
        {
            List<Problem> result = new ArrayList<>(problemNames.size());

            //load file and add to list if not null
            problemNames.parallelStream().forEach(name -> {
                final Problem problem = loadSingleProblem(name);
                if (problem != null)
                {
                    result.add(problem);
                }
            });
            return result;
        }
        logger.info("loadProblems: problemName was NULL!");
        return null;
    }

    private Problem loadSingleProblem(String problemName)
    {
        Problem problem = null;
//        final String filePath = problemName + FILE_TYPE;
        final String filePath = jsonsDir.getPath() + "\\" + problemName + FILE_TYPE;
        try(Reader reader = new BufferedReader(new FileReader(filePath)))
        {
            JsonParser parser = new JsonParser();
            JsonObject fullJsonObj;
            try //parse simple fields from json and check the file's schema
            {
                fullJsonObj = parser.parse(reader).getAsJsonObject();
                validateJson(fullJsonObj);
                problem = gson.fromJson(fullJsonObj, Problem.class);
            } catch (JsonSyntaxException e)
            {
                logger.error("Problem file " + problemName + "'s syntax is wrong.", e);
                return null;
            } catch (JsonParseException e)
            {
                logger.error("Problem file " + problemName + " is missing a field.", e);
                return null;
            }
            problem.setId(problemName);

            getDeviceDict(); //make sure deviceDict != null
            problem.setAllDevices(new HashMap<>(deviceDict));

            JsonObject agentsObj = fullJsonObj.get("agents").getAsJsonObject();
            List<AgentData> agents = parseAgentDataObjects(agentsObj, problem.getPriceScheme(), problem.getGranularity());
            problem.setAllHomes(agents);
        } catch (UnsupportedEncodingException e)
        {
            logger.warn("Problem " + problemName + FILE_TYPE + "'s encoding caused a problem", e);
        } catch (IOException e)
        {
            logger.warn("IOException while parsing Problem " + problemName + FILE_TYPE, e);
        }
        return problem;
    }

    private void validateJson(JsonObject jsonObject) throws JsonParseException
    {
        List<String> requiredProblemFields = Arrays.asList(
                "horizon", "granularity", "priceSchema", "agents");
        List<String> requiredAgentFields = Arrays.asList(
                "neighbors", "backgroundLoad", "houseType", "rules",
                "actuators", "sensors");

        //validate problem fields
        for (String fieldName : requiredProblemFields)
        {
            if (jsonObject.get(fieldName) == null)
            {
                throw new JsonParseException("Required Field Not Found: " + fieldName);
            }
        }

        //validate each agent
        JsonObject agentsObj = jsonObject.get("agents").getAsJsonObject();
        agentsObj.entrySet().forEach(entry -> {
            for (String fieldName : requiredAgentFields)
            {
                if (entry.getValue().getAsJsonObject().get(fieldName) == null)
                {
                    throw new JsonParseException("Required Field Not Found: " + fieldName);
                }
            }
        });
    }

    private List<AgentData> parseAgentDataObjects(JsonObject agentsObj, double[] priceSchema, int granularity)
    {
        List<TempAgentData> tempList = new ArrayList<>(agentsObj.size());
        List<AgentData> agents = new ArrayList<>(agentsObj.size());

        //create TempAgentData objects from json
        agentsObj.entrySet().forEach(entry -> {

            TempAgentData temp = gson.fromJson(entry.getValue(), TempAgentData.class);
            temp.name = entry.getKey();
            tempList.add(temp);
            final AgentData agentData = new AgentData(temp.name, granularity);
            agents.add(agentData);
        });

        //copy and parse fields into AgentData objects
        for (int i = 0; i < tempList.size(); i++)
        {
            TempAgentData temp = tempList.get(i);
            AgentData agent = agents.get(i);
            agent.setPriceScheme(priceSchema);
            temp.copyFieldsToAgentData(agent, deviceDict.get(temp.houseType), agents);
        }
        return agents;
    }

    @Override
    public List<String> getAllProblemNames()
    {
        if (jsonsDir == null) {
            logger.error("jsonsDir is null!");
            return null;
        }
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
        if (deviceDict != null)
        {
            return deviceDict;
        }

        deviceDict = new HashMap<>(3);
//        String deviceDictPath = DEVICE_DICT_FILE_NAME + FILE_TYPE;
        String deviceDictPath = jsonsDir.getPath() + "\\" + DEVICE_DICT_FILE_NAME + FILE_TYPE;

        try(Reader reader = new BufferedReader(new FileReader(deviceDictPath)))
        {
            JsonParser parser = new JsonParser();
            //array of size 3, one for each house type
            JsonArray jsonArr = parser.parse(reader).getAsJsonArray();
            for (int i = 0; i < jsonArr.size(); i++)
            {
                List<Device> allDevices = new ArrayList<>();

                //all devices of house type i
                JsonObject deviceListObj = jsonArr.get(i).getAsJsonObject();
                deviceListObj.entrySet().forEach(entry -> allDevices.add(parseDevice(entry)));
                deviceDict.put(i, allDevices);
            }
        } catch (UnsupportedEncodingException e)
        {
            logger.warn("DeviceDictionary.json's encoding was NOT UTF-8", e);
            return null;
        } catch (IOException e)
        {
            logger.warn("IOException while parsing DeviceDictionary.json", e);
            return null;
        }
        return deviceDict;
    }

    private Device parseDevice(Map.Entry<String, JsonElement> entry)
    {
        Device curr;
        JsonObject jsonObject = entry.getValue().getAsJsonObject();
        if (jsonObject.get("type").getAsString().equals("sensor"))
        {
            curr = gson.fromJson(jsonObject, Sensor.class);
            curr.setName(entry.getKey());
        }
        else //actuator
        {
            curr = gson.fromJson(jsonObject, Actuator.class);
            curr.setName(entry.getKey());

            //parse actions:
            JsonObject actObj = entry.getValue().getAsJsonObject()
                    .get("actions").getAsJsonObject();
            ((Actuator)curr).setActions(parseActions(actObj));
        }
        return curr;
    }

    private List<Action> parseActions(JsonObject actObj)
    {
        List<Action> actions = new ArrayList<>();
        for (Map.Entry<String, JsonElement> actEntry : actObj.entrySet())
        {
            //gson automatically parses inner Effects
            Action act = gson.fromJson(actEntry.getValue(), Action.class);
            act.setName(actEntry.getKey());
            actions.add(act);
        }
        return actions;
    }

    private class TempAgentData{

        public String name;
        @SerializedName("neighbors")
        public String[] neighbors;
        @SerializedName("backgroundLoad")
        public double[] backgroundLoad;
        @SerializedName("houseType")
        public int houseType;
        @SerializedName("rules")
        public String[] rules;
        @SerializedName("actuators")
        public String[] actuators;
        @SerializedName("sensors")
        public String[] sensors;

        public TempAgentData(String[] neighbors, double[] backgroundLoad, int houseType, String[] rules,
                             String[] actuators, String[] sensors)
        {
            this.neighbors = neighbors;
            this.backgroundLoad = backgroundLoad;
            this.houseType = houseType;
            this.rules = rules;
            this.actuators = actuators;
            this.sensors = sensors;
        }

        private void copyFieldsToAgentData(AgentData target, List<Device> deviceList,
                                           List<AgentData> allAgents)
        {
            target.setName(name);
            target.setHouseType(houseType);
            target.setBackgroundLoad(backgroundLoad);

            List<Rule> parsedRules = Arrays.stream(rules)
                    .map(string -> new Rule(string, deviceList))
                    .collect(Collectors.toList());
            target.setRules(parsedRules);

            List<Actuator> acts = copyActuatorsList(deviceList, actuators);
            target.setActuators(acts);

            List<Sensor> sensorsList = copySensorsList(deviceList, sensors);
            target.setSensors(sensorsList);

            List<AgentData> targetNeighbors = Arrays.stream(neighbors)
                    .map(name -> {
                        for (AgentData agent : allAgents)
                        {
                            if (agent.getName().equals(name))
                            {
                                return agent;
                            }
                        }
                        return null;
                    })
                    .filter(agent -> agent != null)
                    .collect(Collectors.toList());
            target.setNeighbors(targetNeighbors);
        }


        private List<Sensor> copySensorsList(List<Device> deviceList, String[] deviceNamesToCopy) {
            List<Sensor> lst = new ArrayList<>();
            for (String name : deviceNamesToCopy)
            {
                lst.addAll(deviceList.stream()
                                   .filter(dev -> dev.getName().equals(name))
                                   .map(dev -> new Sensor((Sensor) dev))
                                   .collect(Collectors.toList()));
            }
            return lst;
        }

        private List<Actuator> copyActuatorsList(List<Device> deviceList, String[] deviceNamesToCopy) {
            List<Actuator> lst = new ArrayList<>();
            for (String name : deviceNamesToCopy)
            {
                lst.addAll(deviceList.stream()
                                   .filter(dev -> dev.getName().equals(name))
                                   .map(dev -> new Actuator((Actuator) dev))
                                   .collect(Collectors.toList()));
            }
            return lst;
        }

    }
}
