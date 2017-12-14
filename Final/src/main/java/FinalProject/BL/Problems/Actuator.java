package FinalProject.BL.Problems;

import org.apache.log4j.Logger;

import java.util.List;
import java.util.Optional;

//@JsonAdapter(Actuator.ActuatorJsonAdapter.class)
public class Actuator extends Device {

    private final static Logger logger = Logger.getLogger(Actuator.class);

//    @SerializedName("actions")
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

//    public class ActuatorJsonAdapter extends TypeAdapter<Actuator>
//    {
//
//        @Override
//        public void write(JsonWriter jsonWriter, Actuator actuator) throws IOException
//        {
//            //TODO
//        }
//
//        @Override
//        public Actuator read(JsonReader jsonReader) throws IOException
//        {
//            jsonReader.beginObject();
//            jsonReader.skipValue();
//            jsonReader.skipValue();
//            jsonReader.skipValue();
//            String subtype = jsonReader.nextString();
//            jsonReader.skipValue();
//            String location = jsonReader.nextString();
//            jsonReader.skipValue();
//            jsonReader.;
//
//            return null;
//        }
//    }
}
