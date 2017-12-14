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

    @Override
    public String toString()
    {
        return "Effect{" +
                "property='" + property + '\'' +
                ", delta=" + delta +
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

        Effect effect = (Effect) o;

        if (Double.compare(effect.getDelta(), getDelta()) != 0)
        {
            return false;
        }
        return getProperty().equals(effect.getProperty());

    }

    @Override
    public int hashCode()
    {
        int result;
        long temp;
        result = getProperty().hashCode();
        temp = Double.doubleToLongBits(getDelta());
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
