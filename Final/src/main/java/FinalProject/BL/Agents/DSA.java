package FinalProject.BL.Agents;
import FinalProject.BL.Problems.*;
import jade.lang.acl.MessageTemplate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class DSA extends SmartHomeAgentBehaviour {

    private static MessageTemplate expectedMessagesTemplate = MessageTemplate.MatchContent("DSA");
    private boolean finished = false;
    private int currentNumberOfIter;
    public static final int START_TICK = 0;
    public final int FINAL_TICK = agent.getAgentData().getBackgroundLoad().length;
    private List<PropertyWithData> allProperties = new ArrayList<>();


    public DSA(String agentName, SmartHomeAgent agent)
    {
        this.agentName = agentName;
        this.agent = agent;
        this.currentNumberOfIter =0;
    }
    @Override
    protected void doIteration() {
        if (agent.isZEROIteration())
        {
            buildScheduleFromScratch();
            agent.setZEROIteration(false);
        }
        else
        {
            tryBuildSceduale();
        }

    }

    private void tryBuildSceduale() {
    }

    private void buildScheduleFromScratch() {
        //classifying the rules by activitness, start creating the prop object
        List <Rule> passiveRules = new ArrayList<>();
        List <Rule> activeRules = new ArrayList<>();
        for (Rule rule : agent.getAgentData().getRules())
        {
            if (rule.isActive())
                activeRules.add(rule);
            else
                passiveRules.add(rule);
        }

        passiveRules.forEach(pRule -> buildPropObj(pRule, true));
        activeRules.forEach(pRule -> buildPropObj(pRule, false));

        SetActuatorsAndSensors();
        Map<PropertyWithData,Integer[]> PropToRange = checkHowLongDeviceNeedToWork();
    }

    private void SetActuatorsAndSensors()
    {
        //add to the prop Objects their related actuators and sensors
        List<Actuator> notFound = new ArrayList<>();
        for(Actuator actuator : agent.getAgentData().getActuators())
        {
            for(Rule rule : agent.getAgentData().getRules())
            {
                //device was found in the rule
                if (rule.getDevice().getName().equals(actuator.getName()))
                {
                    //get the relevant prop object.
                    PropertyWithData prop = allProperties.stream()
                            .filter(x->x.getName().equals(rule.getProperty()))
                            .findFirst().get();

                    //update the actuator
                    prop.actuator = actuator;
                    prop.isLoaction = false;

                    //update the deltot and sensors.
                    for(Action act : actuator.getActions())
                    {
                        matchSensors(act, prop, act.getName().equals("off")? true : false);
                    }
                }
                else
                {
                    notFound.add(actuator);
                }
            }
        }
        //interleave between the left actuators and the location
        for (Actuator actuator : notFound)
        {
            for(Action act : actuator.getActions())
            {
                for(PropertyWithData prop : allProperties.stream()
                                                .filter(p->p.isLoaction==true)
                                                .collect(Collectors.toList()))
                {
                    if (act.getEffects().stream()
                            .anyMatch(e->e.getProperty().equals(prop.name)))
                    {
                        prop.actuator = actuator;
                    }
                }

            }
        }
        //fill the sensors.
        for(PropertyWithData prop : allProperties.stream()
                .filter(p->p.isLoaction==true)
                .collect(Collectors.toList()))
        {
            for(Action act : prop.actuator.getActions())
            {
                matchSensors(act, prop, act.getName().equals("off")? true : false);
            }
        }
    }

    private void matchSensors(Action act, PropertyWithData prop, boolean isOffline)
    {
        List<Sensor> sensors = agent.getAgentData().getSensors();
        for(Effect effect : act.getEffects())
        {
            if (isOffline)
            {
                if (effect.getProperty().equals(prop.name))
                {
                    prop.deltaWhenWorkOffline = effect.getDelta();
                    prop.sensor = sensors.stream()
                            .filter(x->x.getSensingProperties().contains(prop.name))
                            .findFirst().get();
                }
                else
                {
                    prop.relatedSensorsWhenWorkOffline.put(effect.getProperty(),effect.getDelta());
                }
            }
            else
            {
                if (effect.getProperty().equals(prop.name)) {
                    prop.deltaWhenWork = effect.getDelta();
                } else {
                    prop.relatedSensors.put(effect.getProperty(), effect.getDelta());
                }
            }
        }


    }

    private void buildPropObj(Rule rule, boolean isPassive) {
        PropertyWithData prop = allProperties.stream()
                .filter(x->x.name.equals(rule.getProperty())).findFirst().get();
        if (prop == null)
        {
            prop = new PropertyWithData();
            prop.name = rule.getProperty();
            allProperties.add(prop);
        }
        if (isPassive)
        {
            switch (rule.getPrefixType())
            {
                case EQ:
                    prop.min = rule.getRelationValue();
                    break;
                case GEQ:
                    prop.max = rule.getRelationValue();
                    break;
                case LEQ:
                    prop.min = rule.getRelationValue();
                    break;
                case GT:
                    prop.max = rule.getRelationValue();
                    break;
                case LT:
                    prop.min = rule.getRelationValue();
                    break;
            }
        }
        else
        {
            switch (rule.getPrefixType())
            {

                case EQ:
                    prop.targetValue = rule.getRelationValue() + "=";
                    break;
                case GEQ:
                    prop.targetValue = rule.getRelationValue() + "+=";
                    break;
                case LEQ:
                    prop.targetValue = rule.getRelationValue() + "-=";
                    break;
                case GT:
                    prop.targetValue = rule.getRelationValue() + "+";
                    break;
                case LT:
                    prop.targetValue = rule.getRelationValue() + "-";
                    break;
            }
        }
    }


    private Map<PropertyWithData,Integer[]> checkHowLongDeviceNeedToWork()
    {
        Map<PropertyWithData,Integer[]> PropToRanges = new HashMap<>();
        for(PropertyWithData prop : allProperties.stream()
                .filter(p->p.isPassiveOnly==false)
                .collect(Collectors.toList()))
        {
            //TODO build range
        }


        return PropToRanges;
    }

    @Override
    protected void sendIterationToCollector() {

    }

    @Override
    public void action() {
        this.agent.printLog("Starting work on Iteration: " + this.currentNumberOfIter);
        this.agent.buildSchedule();
        doIteration();
        this.currentNumberOfIter ++;

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            agent.printLog(e.getMessage());
        }
    }

    @Override
    public boolean done() {
        return currentNumberOfIter+1 == agent.getAgentData().getNumOfIterations() ? true : false;
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T,Object> keyExtractor) {
        Map<Object,String> seen = new ConcurrentHashMap<>();
        return t -> seen.put(keyExtractor.apply(t), "") == null;
    }

    private class PropertyWithData
    {
        private String name;
        private double min;
        private double max;
        private String targetValue;
        private Actuator actuator;
        private Sensor sensor;
        private boolean isPassiveOnly;
        private double deltaWhenWork;
        private double deltaWhenWorkOffline;
        private boolean isLoaction = true;
        private  Map<String,Double> relatedSensors = new HashMap<>();
        private  Map<String,Double> relatedSensorsWhenWorkOffline = new HashMap<>();


        public PropertyWithData () {}

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

        public String getTargetValue() {
            return targetValue;
        }

        public void setTargetValue(String targetValue) {
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

        public double getDeltaWhenWork() {
            return deltaWhenWork;
        }

        public void setDeltaWhenWork(double deltaWhenWork) {
            this.deltaWhenWork = deltaWhenWork;
        }

        public Map<String, Double> getRelatedSensors() {
            return relatedSensors;
        }

        public void setRelatedSensors(Map<String, Double> relatedSensors) {
            this.relatedSensors = relatedSensors;
        }

    }
}
