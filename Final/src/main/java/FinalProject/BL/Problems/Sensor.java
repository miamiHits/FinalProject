package FinalProject.BL.Problems;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Sensor extends Device {

    @SerializedName("current_state")
    private double currentState;
    @SerializedName("sensing_properties")
    private List<String> sensingProperties;

    public Sensor(String name, String subtype, String location,
                  double state, List<String> sensingProps)
    {
        super(name, subtype, location);
        currentState = state;
        sensingProperties = sensingProps;
    }

    public double getCurrentState()
    {
        return currentState;
    }

    public void setCurrentState(double currentState)
    {
        this.currentState = currentState;
    }

    public List<String> getSensingProperties()
    {
        return sensingProperties;
    }

    public void setSensingProperties(List<String> sensingProperties)

    {
        this.sensingProperties = sensingProperties;
    }

    public void change(double value)
    {
        currentState = value;
    }

    @Override
    public String toString()
    {
        return "Sensor{" +
                "currentState=" + currentState +
                ", sensingProperties=" + sensingProperties +
                '}';
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

        Sensor sensor = (Sensor) o;

        if (Double.compare(sensor.getCurrentState(), getCurrentState()) != 0)
        {
            return false;
        }
        return getSensingProperties() != null ? getSensingProperties().equals(sensor.getSensingProperties()) : sensor.getSensingProperties() == null;

    }

    @Override
    public int hashCode()
    {
        int result;
        long temp;
        temp = Double.doubleToLongBits(getCurrentState());
        result = (int) (temp ^ (temp >>> 32));
        result = 31 * result + (getSensingProperties() != null ? getSensingProperties().hashCode() : 0);
        return result;
    }
}
