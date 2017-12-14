package FinalProject.BL.Problems;

import java.util.List;

public class AgentData {

    private String name;

    private List<AgentData> neighbors;
    private double[] backgroundLoad;
    private int houseType;
    private List<Rule> rules;
    private List<Actuator> actuators;
    private List<Sensor> sensors;

    public AgentData(String name, List<AgentData> neighbors, double[] backgroundLoad, int houseType,
                     List<Rule> rules, List<Actuator> actuators, List<Sensor> sensors)
    {
        this.name = name;
        this.neighbors = neighbors;
        this.backgroundLoad = backgroundLoad;
        this.houseType = houseType;
        this.rules = rules;
        this.actuators = actuators;
        this.sensors = sensors;
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
}
