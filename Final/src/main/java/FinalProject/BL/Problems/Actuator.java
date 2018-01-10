package FinalProject.BL.Problems;

import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Actuator extends Device {

    private final static Logger logger = Logger.getLogger(Actuator.class);
    private List<Action> actions;

    public Actuator(String name, String subtype, String location,
                    List<Action> actionList)
    {
        super(name, subtype, location);
        actions = actionList;
    }

    public Actuator(Actuator other) {
        super(other);
        this.actions = other.getActions().stream()
                .map(Action::new).collect(Collectors.toList());
    }

    public List<Action> getActions()
    {
        return actions;
    }

    public void setActions(List<Action> actions)
    {
        this.actions = actions;
    }

    @Override
    public String toString()
    {
        return "Actuator{" +
                "actions=" + actions +
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

        Actuator actuator = (Actuator) o;

        return getActions() != null ? getActions().equals(actuator.getActions()) : actuator.getActions() == null;

    }

    @Override
    public int hashCode()
    {
        return getActions() != null ? getActions().hashCode() : 0;
    }

    public void act(Map<Sensor, Double> toSend) {
        for(Map.Entry<Sensor, Double> entry : toSend.entrySet())
        {
            entry.getKey().change(entry.getValue());
        }

    }
}
