package FinalProject.BL.Agents;

import FinalProject.BL.Problems.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AlgorithmDataHelper
{
    public double totalPriceConsumption=0;
    public  Map<Actuator, List<Integer>> DeviceToTicks = new HashMap<>();
    public List<PropertyWithData> allProperties;
    private SmartHomeAgent agent;

    public AlgorithmDataHelper (SmartHomeAgent agent)
    {
        this.agent = agent;
        allProperties = new ArrayList<>();
    }

    public PropertyWithData createNewProp()
    {
        return  new PropertyWithData();
    }


    public void buildPropObj(Rule rule, boolean isPassive) {
        AlgorithmDataHelper.PropertyWithData prop= null;
        if (allProperties.size() > 0)
        {
            prop = allProperties.stream().filter(p -> p.getName().equals(rule.getProperty()))
                    .findFirst().orElse(null);
        }
        if (prop == null)
        {
            prop = createNewProp();
            prop.setName(rule.getProperty());
            allProperties.add(prop);
        }
        if (isPassive)
        {
            switch (rule.getPrefixType())
            {
                case EQ:
                    prop.setMin(rule.getRuleValue());
                    break;
                case GEQ:
                    prop.setMin(rule.getRuleValue());
                    break;
                case LEQ:
                    prop.setMax(rule.getRuleValue());
                    break;
                case GT:
                    prop.setMin(rule.getRuleValue());
                    break;
                case LT:
                    prop.setMax(rule.getRuleValue());
                    break;
            }
        }
        else
        {
            prop.setPrefix(rule.getPrefix());
            prop.setRt(rule.getPrefixType());
            prop.setTargetTick(rule.getRelationValue());
            prop.setTargetValue(rule.getRuleValue());
        }
    }

    public void SetActuatorsAndSensors()
    {
        Map <String, Actuator> map = new HashMap<String, Actuator>();
        //iterate over the rules list.
        //Check if the device was found in the rules
        List<Actuator> actuators = agent.getAgentData().getActuators();
        for (int i = 0, actuatorsSize = actuators.size(); i < actuatorsSize; i++) {
            Actuator a = actuators.get(i);
            agent.getAgentData().getRules().stream().filter(r -> r.getDevice() != null)
                    .filter(r -> r.getDevice().getName().equals(a.getName()))
                    .findFirst().ifPresent(r -> map.put(r.getProperty(), a));
        }

        for(Map.Entry<String, Actuator> entry : map.entrySet())
        {
            //get the relevant prop object.
            AlgorithmDataHelper.PropertyWithData prop = allProperties.stream()
                    .filter(x->x.getName().equals(entry.getKey()))
                    .findFirst().get();

            //update the actuator
            prop.setActuator(entry.getValue());
            prop.setLoaction(false);

            //update the deltot and sensors.
            for(Action act : entry.getValue().getActions())
            {
                matchSensors(act, prop, act.getName().equals("off")? true : false);
            }
        }

        //Added all the Actuators that we did npt found in the rules (cause they are locations)
        List<Actuator> notFound = agent.getAgentData().getActuators().stream()
                .filter(a -> !map.containsValue(a)).collect(Collectors.toList());


        //interleave between the left actuators and the location
        for (Actuator actuator : notFound)
        {
            for(Action act : actuator.getActions())
            {
                for(AlgorithmDataHelper.PropertyWithData prop : allProperties.stream()
                        .filter(p->p.isLoaction()==true)
                        .collect(Collectors.toList()))
                {
                    if (act.getEffects().stream()
                            .anyMatch(e->e.getProperty().equals(prop.getName())))
                    {
                        prop.setActuator(actuator);
                        break;
                    }
                }

            }
        }
        //fill the sensors.
        for(AlgorithmDataHelper.PropertyWithData prop : allProperties.stream()
                .filter(p->p.isLoaction()==true)
                .collect(Collectors.toList()))
        {
            for(Action act : prop.getActuator().getActions())
            {
                matchSensors(act, prop, act.getName().equals("off")? true : false);
            }
        }
    }

    private void matchSensors(Action act, AlgorithmDataHelper.PropertyWithData prop, boolean isOffline)
    {
        List<Sensor> sensors = agent.getAgentData().getSensors();
        for(Effect effect : act.getEffects())
        {
            if (isOffline)
            {
                if (effect.getProperty().equals(prop.getName()))
                {
                    prop.setDeltaWhenWorkOffline(effect.getDelta());
                    prop.setSensor(sensors.stream()
                            .filter(x->x.getSensingProperties().contains(prop.getName()))
                            .findFirst().get());
                }
                else
                {
                    prop.relatedSensorsWhenWorkOfflineDelta.put(effect.getProperty(),effect.getDelta());
                }
            }
            else
            {
                if (effect.getProperty().equals(prop.getName())) {
                    prop.setDeltaWhenWork(effect.getDelta());
                } else {
                    prop.relatedSensorsDelta.put(effect.getProperty(), effect.getDelta());
                }
            }
        }


    }

    public double calcPrice(double[] powerConsumption) {
        double res = 0 ;
        double [] backgroundLoad = agent.getAgentData().getBackgroundLoad();
        double [] priceScheme = agent.getAgentData().getPriceScheme();
        for (int i=0 ; i<backgroundLoad.length; ++i)
        {
            double temp =  Double.sum(powerConsumption[i], priceScheme[i]);
            double temp2 = Double.sum(temp,backgroundLoad[i] );
            res = Double.sum(temp2, res);
            // Double.sum(res, Double.sum(backgroundLoad[i], Double.sum(powerConsumption[i], priceScheme[i])));
        }
        return res;
    }

    public int drawCoin() {
        int[] notRandomNumbers = new int [] {0,0,0,0,1,1,1,1,1,1};
        double idx = Math.floor(Math.random() * notRandomNumbers.length);
        return notRandomNumbers[(int) idx];
    }

    public List<Integer> calcNewTicks(Actuator actuator){
        List<Integer> currTicks = DeviceToTicks.get(actuator);


        return null;
    }

    public class PropertyWithData
    {
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

        public double getPowerConsumption() {
            return powerConsumption;
        }

        public void setPowerConsumption(double powerConsumption) {
            this.powerConsumption = powerConsumption;
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

        private double deltaWhenWorkOffline;
        private double powerConsumption;
        private boolean isLoaction = true;
        public  Map<String,Double> relatedSensorsDelta = new HashMap<>();
        public  Map<String,Double> relatedSensorsWhenWorkOfflineDelta = new HashMap<>();


        public PropertyWithData () {}

        public boolean canBeModified (double amountOfChange)
        {
            double newState = sensor.getCurrentState() + amountOfChange;
            if (!Double.isNaN(max) && newState > max || (!Double.isNaN(min) && newState > min))
                return false;

            return true;
        }


    }
}
