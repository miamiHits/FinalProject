package FinalProject.BL.Problems;

public class Effect {

    private String property;
    private double delta;

    public Effect(String property, double delta)
    {
        this.property = property;
        this.delta = delta;
    }

    public String getProperty()
    {
        return property;
    }

    public void setProperty(String property)
    {
        this.property = property;
    }

    public double getDelta()
    {
        return delta;
    }

    public void setDelta(double delta)
    {
        this.delta = delta;
    }
}
