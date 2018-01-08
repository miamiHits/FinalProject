package FinalProject.BL.Problems;

import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Actuator extends Device {

    private final static Logger logger = Logger.getLogger(Actuator.class);
    private List<Action> actions;

    public Actuator(String name, String subtype, String location,
                    List<Action> actionList)
    {
        super(name, subtype, location);
        actions = actionList;
    }

    public List<Action> getActions()
    {
        return actions;
    }

    public void setActions(List<Action> actions)
    {
        this.actions = actions;
    }

    public void act(List<Sensor> sensors)
    {
        sensors.forEach(sens -> {
            List<String> sensingProps = sens.getSensingProperties();
            for (String prop : sensingProps)
            {
                Optional<Double> deltaOpt = actions.stream()
                        .map(act -> act.getEffectWithProperty(prop))
                        .filter(eff -> eff != null)
                        .map(Effect::getDelta)
                        .findAny(); //there should be no more than 1

                if (deltaOpt.isPresent())
                {
                    sens.change(deltaOpt.get());
                }
                else
                {
                    logger.warn("Actuator " + name + " has no action with property " + prop);
                }
            }
        });
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
