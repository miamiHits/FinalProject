package FinalProject.BL.Problems;

import java.util.List;

public class Sensor extends Device {

    private double currentState;
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
        currentState += value;
    }
}
