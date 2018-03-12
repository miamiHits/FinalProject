package FinalProject.BL.DataObjects;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Action implements Serializable
{


    private String name;
    @SerializedName("power_consumed")
    private double powerConsumption;
    @SerializedName("effects")
    private List<Effect> effects;

    public Action(String name, double powerConsumption, List<Effect> effects)
    {
        this.name = name;
        this.powerConsumption = powerConsumption;
        this.effects = effects;
    }

    public Action(Action other) {
        this.name = other.getName();
        this.powerConsumption = other.getPowerConsumption();
        this.effects = new ArrayList<>(other.getEffects().size());
        this.effects.addAll(other.effects);
    }


    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public double getPowerConsumption()
    {
        return powerConsumption;
    }

    public void setPowerConsumption(double powerConsumption)
    {
        this.powerConsumption = powerConsumption;
    }

    public List<Effect> getEffects()
    {
        return effects;
    }

    public void setEffects(List<Effect> effects)
    {
        this.effects = effects;
    }

    public Effect getEffectWithProperty(String property)
    {
        for (Effect eff : effects)
        {
            if (eff.getProperty().equals(property))
            {
                return eff;
            }
        }
        return null;
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

        Action action = (Action) o;

        if (Double.compare(action.getPowerConsumption(), getPowerConsumption()) != 0)
        {
            return false;
        }
        if (getName() != null ? !getName().equals(action.getName()) : action.getName() != null)
        {
            return false;
        }
        return getEffects() != null ? getEffects().equals(action.getEffects()) : action.getEffects() == null;

    }

    @Override
    public int hashCode()
    {
        int result;
        long temp;
        result = getName() != null ? getName().hashCode() : 0;
        temp = Double.doubleToLongBits(getPowerConsumption());
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (getEffects() != null ? getEffects().hashCode() : 0);
        return result;
    }
}

