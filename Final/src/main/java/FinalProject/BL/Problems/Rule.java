package FinalProject.BL.Problems;

import org.apache.log4j.Logger;

import java.util.List;

public class Rule {

    private static final Logger logger = Logger.getLogger(Rule.class);

    private boolean isActive;
    private Device device = null;
    private String location = null;
    private String property;
    private double ruleValue;
    private RelationType prefixType;
    private Prefix prefix;
    private double relationValue;

    public Rule(boolean isActive, Device device, String location, String property, double ruleValue,
                RelationType prefixType, Prefix prefix, double relationValue)
    {
        this.isActive = isActive;
        this.device = device;
        this.location = location;
        this.property = property;
        this.ruleValue = ruleValue;
        this.prefixType = prefixType;
        this.prefix = prefix;
        this.relationValue = relationValue;
    }

    public Rule(String ruleAsString, List<Device> deviceDict)
    {
        String[] split = ruleAsString.split(" ");
        isActive = split[0].equals("1");
        location = split[1];
        device = parseDevice(split[1], deviceDict);
        property = split[2];
        prefixType = parseRelationType(split[3]);
        ruleValue = Double.parseDouble(split[4]);

        if (isActive && split.length >= 7)
        {
            prefix = parsePrefix(split[5]);
            relationValue = Double.parseDouble(split[6]);
        }
    }

    private Device parseDevice(String name, List<Device> deviceDict)
    {
        for (Device dev : deviceDict)
        {
            if (name.equals(dev.getName()))
            {
                return dev;
            }
        }
        return null;
    }

    private Prefix parsePrefix(String prefixStr)
    {
        switch (prefixStr.toLowerCase())
        {
            case "before":  return Prefix.BEFORE;
            case "after":   return Prefix.AFTER;
            case "at":      return Prefix.AT;
            default:
                logger.info("Could not parse prefix from rule: " + prefixStr);
                return null;
        }
    }

    private RelationType parseRelationType(String relationTypeStr)
    {
        switch (relationTypeStr.toLowerCase())
        {
            case "eq":  return RelationType.EQ;
            case "geq": return RelationType.GEQ;
            case "leq": return RelationType.LEQ;
            case "gt": return RelationType.GT;
            case "lt": return RelationType.LT;
            default:
                logger.info("Could not parse relationType from rule: " + relationTypeStr);
                return null;
        }
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

    public String getLocation()
    {
        return location;
    }

    public void setLocation(String location)
    {
        this.location = location;
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
        if ((getDevice() == null && rule.getDevice() != null) ||
                (rule.getDevice() == null && getDevice() != null))
        {
            return false;
        }
        if (getDevice() != null && rule.getDevice() != null &&
                !getDevice().equals(rule.getDevice()))
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
