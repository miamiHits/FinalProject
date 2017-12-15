package FinalProject.BL.Problems;

import java.util.Arrays;
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

    //Used only for Json parsing!
    public AgentData(String name)
    {
        this.name = name;
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

    //TODO: fix
//    @Override
//    public String toString()
//    {
//        return "AgentData{" +
//                "name='" + name + '\'' +
//                ", neighbors=" + neighbors +
//                ", backgroundLoad=" + Arrays.toString(backgroundLoad) +
//                ", houseType=" + houseType +
//                ", rules=" + rules +
//                ", actuators=" + actuators +
//                ", sensors=" + sensors +
//                '}';
//    }

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
        if (!getNeighbors().equals(agentData.getNeighbors()))
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
    public int hashCode()
    {
        int result = getName().hashCode();
        result = 31 * result + getNeighbors().hashCode();
        result = 31 * result + Arrays.hashCode(getBackgroundLoad());
        result = 31 * result + getHouseType();
        result = 31 * result + getRules().hashCode();
        result = 31 * result + getActuators().hashCode();
        result = 31 * result + getSensors().hashCode();
        return result;
    }
}
