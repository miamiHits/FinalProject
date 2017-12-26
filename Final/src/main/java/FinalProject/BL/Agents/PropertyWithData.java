package FinalProject.BL.Agents;

import FinalProject.BL.Problems.Actuator;
import FinalProject.BL.Problems.Prefix;
import FinalProject.BL.Problems.RelationType;
import FinalProject.BL.Problems.Sensor;

import java.util.HashMap;
import java.util.Map;

public class PropertyWithData {
    private String name;
    private double min;
    private double max;
    private double targetValue;
    private Actuator actuator;
    private Sensor sensor;
    private boolean isPassiveOnly = false;
    private Prefix prefix;
    private RelationType rt;
    private double targetTick;
    private double deltaWhenWork;
    private double powerConsumedInWork;
    private double deltaWhenWorkOffline;
    private boolean isLoaction = true;
    public  Map<String,Double> relatedSensorsDelta = new HashMap<>();
    public  Map<String,Double> relatedSensorsWhenWorkOfflineDelta = new HashMap<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public double getTargetValue() {
        return targetValue;
    }

    public void setTargetValue(double targetValue) {
        this.targetValue = targetValue;
    }

    public Actuator getActuator() {
        return actuator;
    }

    public void setActuator(Actuator actuator) {
        this.actuator = actuator;
    }

    public Sensor getSensor() {
        return sensor;
    }

    public void setSensor(Sensor sensor) {
        this.sensor = sensor;
    }

    public boolean isPassiveOnly() {
        return isPassiveOnly;
    }

    public void setPassiveOnly(boolean passiveOnly) {
        isPassiveOnly = passiveOnly;
    }

    public Prefix getPrefix() {
        return prefix;
    }

    public void setPrefix(Prefix prefix) {
        this.prefix = prefix;
    }

    public RelationType getRt() {
        return rt;
    }

    public void setRt(RelationType rt) {
        this.rt = rt;
    }

    public double getTargetTick() {
        return targetTick;
    }

    public void setTargetTick(double targetTick) {
        this.targetTick = targetTick;
    }

    public double getDeltaWhenWork() {
        return deltaWhenWork;
    }

    public void setDeltaWhenWork(double deltaWhenWork) {
        this.deltaWhenWork = deltaWhenWork;
    }

    public double getDeltaWhenWorkOffline() {
        return deltaWhenWorkOffline;
    }

    public void setDeltaWhenWorkOffline(double deltaWhenWorkOffline) {
        this.deltaWhenWorkOffline = deltaWhenWorkOffline;
    }

    public boolean isLoaction() {
        return isLoaction;
    }

    public void setLoaction(boolean loaction) {
        isLoaction = loaction;
    }

    public Map<String, Double> getRelatedSensorsDelta() {
        return relatedSensorsDelta;
    }

    public Map<String, Double> getRelatedSensorsWhenWorkOfflineDelta() {
        return relatedSensorsWhenWorkOfflineDelta;
    }

    public double getPowerConsumedInWork() {
        return powerConsumedInWork;
    }

    public void setPowerConsumedInWork(double powerConsumedInWork) {
        this.powerConsumedInWork = powerConsumedInWork;
    }



    public PropertyWithData () {}

    public boolean canBeModified (double amountOfChange)
    {
        double newState = sensor.getCurrentState() + amountOfChange;
        if (!Double.isNaN(max) && newState > max || (!Double.isNaN(min) && newState > min))
            return false;

        return true;
    }





}
