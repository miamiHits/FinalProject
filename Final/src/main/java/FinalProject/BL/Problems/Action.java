package FinalProject.BL.Problems;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Action {


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
}

