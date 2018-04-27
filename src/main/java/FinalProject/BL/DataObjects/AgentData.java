package FinalProject.BL.DataObjects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AgentData implements Serializable{

    private String name;
    private List<AgentData> neighbors;
    private double[] backgroundLoad;
    private int houseType;
    private List<Rule> rules;
    private List<Actuator> actuators;
    private List<Sensor> sensors;
    private int numOfIterations;
    private double[] priceScheme;
    private int granularity;


    public AgentData(String name, List<AgentData> neighbors, double[] backgroundLoad, int houseType, List<Rule> rules,
                     List<Actuator> actuators, List<Sensor> sensors, int numOfIterations, double[] priceScheme,
                     int granularity)
    {
        this.name = name;
        this.neighbors = neighbors;
        this.backgroundLoad = backgroundLoad;
        this.houseType = houseType;
        this.rules = rules;
        this.actuators = actuators;
        this.sensors = sensors;
        this.numOfIterations = numOfIterations;
        this.priceScheme = priceScheme;
        this.granularity = granularity;
    }

    /**
     * Used only for Json parsing!
     * @param name
     * @param granularity
     */
    public AgentData(String name, int granularity)
    {
        this.name = name;
        this.granularity = granularity;
    }

    public AgentData(AgentData other) {
        this.name = other.name;
        this.neighbors = new ArrayList<>(other.neighbors);
        this.backgroundLoad = Arrays.copyOf(other.backgroundLoad, other.backgroundLoad.length);
        this.houseType = other.houseType;
        this.rules = other.getRules().stream().map(rule -> new Rule(rule)).collect(Collectors.toList());
        this.sensors = other.sensors.stream().map(sensor -> new Sensor(sensor)).collect(Collectors.toList());
        this.actuators = other.actuators.stream().map(actuator -> new Actuator(actuator)).collect(Collectors.toList());
        this.numOfIterations = other.numOfIterations;
        this.priceScheme = Arrays.copyOf(other.priceScheme, other.priceScheme.length);
        this.granularity = other.granularity;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public List<AgentData> getNeighbors()
    {
        return neighbors;
    }

    public void setNeighbors(List<AgentData> neighbors)
    {
        this.neighbors = neighbors;
    }

    public double[] getBackgroundLoad()
    {
        return backgroundLoad;
    }

    public void setBackgroundLoad(double[] backgroundLoad)
    {
        this.backgroundLoad = backgroundLoad;
    }

    public int getHouseType()
    {
        return houseType;
    }

    public void setHouseType(int houseType)
    {
        this.houseType = houseType;
    }

    public List<Rule> getRules()
    {
        return rules;
    }

    public void setRules(List<Rule> rules)
    {
        this.rules = rules;
    }

    public List<Actuator> getActuators()
    {
        return actuators;
    }

    public void setActuators(List<Actuator> actuators)
    {
        this.actuators = actuators;
    }

    public List<Sensor> getSensors()
    {
        return sensors;
    }

    public void setSensors(List<Sensor> sensors)
    {
        this.sensors = sensors;
    }

    public int getNumOfIterations() {
        return numOfIterations;
    }

    public void setNumOfIterations(int numOfIterations) {
        this.numOfIterations = numOfIterations;
    }

    public double[] getPriceScheme() {
        return priceScheme;
    }

    public void setPriceScheme(double[] priceScheme) {
        this.priceScheme = priceScheme;
    }

    public int getGranularity()
    {
        return granularity;
    }

    public void setGranularity(int granularity)
    {
        this.granularity = granularity;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        AgentData agentData = (AgentData) o;

        if (getHouseType() != agentData.getHouseType())
        {
            return false;
        }
        if (!getName().equals(agentData.getName()))
        {
            return false;
        }

        List<String> otherNeighbors = agentData.getNeighbors().stream()
                .map(AgentData::getName).collect(Collectors.toList());
        for(AgentData neighbor : neighbors)
        {
            if (!otherNeighbors.contains(neighbor.getName()))
            {
                return false;
            }
        }
        if (otherNeighbors.size() != neighbors.size())
        {
            return false;
        }

        if (!Arrays.equals(getBackgroundLoad(), agentData.getBackgroundLoad()))
        {
            return false;
        }
        if (!getRules().equals(agentData.getRules()))
        {
            return false;
        }
        if (!getActuators().equals(agentData.getActuators()))
        {
            return false;
        }
        return getSensors().equals(agentData.getSensors());

    }

    @Override
    public String toString()
    {
        List<String> neighborsNames = neighbors.stream()
                .map(AgentData::getName).collect(Collectors.toList());

        return "AgentData{" +
                "name='" + name + '\'' +
                ", neighbors=" + neighborsNames +
                ", backgroundLoad=" + Arrays.toString(backgroundLoad) +
                ", houseType=" + houseType +
                ", rules=" + rules +
                ", actuators=" + actuators +
                ", sensors=" + sensors +
                '}';
    }
}
