package FinalProject.BL.Problems;

public class Rule {

    private boolean isActive;
    private Device device;
    private String property;
    private double ruleValue;
    private RelationType prefixType;
    private Prefix prefix;
    private double relationValue;

    public Rule(boolean isActive, Device device, String property, double ruleValue,
                RelationType prefixType, Prefix prefix, double relationValue)
    {
        this.isActive = isActive;
        this.device = device;
        this.property = property;
        this.ruleValue = ruleValue;
        this.prefixType = prefixType;
        this.prefix = prefix;
        this.relationValue = relationValue;
    }

    public Rule(String ruleAsString)
    {
        //TODO: parse string
    }

    public boolean isActive()
    {
        return isActive;
    }

    public void setActive(boolean active)
    {
        isActive = active;
    }

    public Device getDevice()
    {
        return device;
    }

    public void setDevice(Device device)
    {
        this.device = device;
    }

    public String getProperty()
    {
        return property;
    }

    public void setProperty(String property)
    {
        this.property = property;
    }

    public double getRuleValue()
    {
        return ruleValue;
    }

    public void setRuleValue(double ruleValue)
    {
        this.ruleValue = ruleValue;
    }

    public RelationType getPrefixType()
    {
        return prefixType;
    }

    public void setPrefixType(RelationType prefixType)
    {
        this.prefixType = prefixType;
    }

    public Prefix getPrefix()
    {
        return prefix;
    }

    public void setPrefix(Prefix prefix)
    {
        this.prefix = prefix;
    }

    public double getRelationValue()
    {
        return relationValue;
    }

    public void setRelationValue(double relationValue)
    {
        this.relationValue = relationValue;
    }

    @Override
    public String toString()
    {
        return "Rule{" +
                "isActive=" + isActive +
                ", device=" + device +
                ", property='" + property + '\'' +
                ", ruleValue=" + ruleValue +
                ", prefixType=" + prefixType +
                ", prefix=" + prefix +
                ", relationValue=" + relationValue +
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

        Rule rule = (Rule) o;

        if (isActive() != rule.isActive())
        {
            return false;
        }
        if (Double.compare(rule.getRuleValue(), getRuleValue()) != 0)
        {
            return false;
        }
        if (Double.compare(rule.getRelationValue(), getRelationValue()) != 0)
        {
            return false;
        }
        if (!getDevice().equals(rule.getDevice()))
        {
            return false;
        }
        if (!getProperty().equals(rule.getProperty()))
        {
            return false;
        }
        if (getPrefixType() != rule.getPrefixType())
        {
            return false;
        }
        return getPrefix() == rule.getPrefix();

    }

    @Override
    public int hashCode()
    {
        int result;
        long temp;
        result = (isActive() ? 1 : 0);
        result = 31 * result + getDevice().hashCode();
        result = 31 * result + getProperty().hashCode();
        temp = Double.doubleToLongBits(getRuleValue());
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + getPrefixType().hashCode();
        result = 31 * result + getPrefix().hashCode();
        temp = Double.doubleToLongBits(getRelationValue());
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
