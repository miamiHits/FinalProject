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
}
