package FinalProject.BL.Agents;
import FinalProject.BL.IterationData.AgentIterationData;
import FinalProject.BL.Problems.*;
import jade.lang.acl.ACLMessage;
import org.apache.log4j.Logger;
import java.util.*;
import java.util.stream.Collectors;

import static FinalProject.BL.DataCollection.PowerConsumptionUtils.calculateTotalConsumptionWithPenalty;

public class DSA extends SmartHomeAgentBehaviour {

    private boolean finished = false;
    private int currentNumberOfIter;
    public static final int START_TICK = 0;
    public int FINAL_TICK;
    public AgentIterationData agentIterationData;
    private final static Logger logger = Logger.getLogger(DSA.class);
    private AlgorithmDataHelper helper;

    public DSA()
    {
        super();//invoke the Behaviour default constructor
    }

    public DSA(SmartHomeAgent agent)
    {
        this.agent = agent;
        this.currentNumberOfIter =0;
        this.FINAL_TICK = agent.getAgentData().getBackgroundLoad().length -1;
        this.helper = new AlgorithmDataHelper(agent);
    }
    @Override
    protected void doIteration() {
        if (agent.isZEROIteration())
        {
            logger.info("Starting build schedule");
            buildScheduleFromScratch();
            agent.setZEROIteration(false);
        }
        else
        {
            this.currentNumberOfIter ++;
            List<ACLMessage> messageList = waitForNeighbourMessages();
            parseMessages(messageList);
            tryBuildSchedule();
        }

    }

    private void tryBuildSchedule() {

        boolean buildNewShed = drawCoin() == 1 ? true : false;
        if (buildNewShed)
        {
            helper.calcPriceSchemeForAllNeighbours();
            for (Actuator act : helper.getDeviceToTicks().keySet())
            {
                List<Integer> newProposeTicks = calcNewTicks(act);
                helper.getDeviceToTicks().put(act, newProposeTicks);
            }
            double[] powerConsumption = buildNewScheduleAccordingToNewTicks();
            double price = calcPrice(powerConsumption);
            helper.totalPriceConsumption = price;
            agentIterationData = new AgentIterationData(currentNumberOfIter, agent.getName(),price, powerConsumption);
            agent.setCurrIteration(agentIterationData);

            //TODO: Update the best iteration.
        }
        else{

            return;
        }

    }

    public int drawCoin() {
        int[] notRandomNumbers = new int [] {0,0,0,0,1,1,1,1,1,1};
        double idx = Math.floor(Math.random() * notRandomNumbers.length);
        return notRandomNumbers[(int) idx];
    }

    public List<Integer> calcNewTicks(Actuator actuator){
        double bestPrice=0;
        List<Integer> newTicks = new ArrayList<>();
        //get the related prop
        PropertyWithData prop =null;
        for(PropertyWithData p : helper.getAllProperties())
        {
            if (p.getActuator().getName().equals(actuator.getName()))
            {
                prop = p;
            }
            else{
                logger.error("Cannot find property with this actuator" + actuator.getName());
            }
        }

        double ticksToWork = helper.calcHowLongDeviceNeedToWork(prop);
        List<Integer> rangeForWork = new ArrayList<>();
        switch (prop.getPrefix())
        {
            case BEFORE: //Include the hour
                for (int i=0; i<= prop.getTargetTick(); ++i)
                {
                    rangeForWork.add(i);
                }
                break;
            case AFTER: // Hour and above (not included)
                for (int i= (int) prop.getTargetTick()+1 ; i<= agent.getAgentData().getBackgroundLoad().length ; ++i)
                {
                    rangeForWork.add(i);
                }
                break;
            case AT:
                rangeForWork.add((int) prop.getTargetTick());
                break;
        }

        //remove the "rush" ticks
        helper.getRushTicks().forEach(rT -> {
            rangeForWork.stream().filter(mT -> rT == mT).forEach(rangeForWork::remove);
        });

        List<List<Integer>> subsets = new ArrayList<>();
        for (int i = 0; i <Math.ceil(rangeForWork.size() / ticksToWork); i++)
        {
            subsets.add(new ArrayList<>((int) ticksToWork));
            helper.solve(rangeForWork.stream().mapToInt(j->j).toArray(), (int) ticksToWork, 0, subsets);
        }

        //get the prev powerCon array
        double [] refactoredPowerConsumption = agent.getCurrIteration().getPowerConsumptionPerTick();
        //get the specific tick this device work in
        List<Integer> prevTicks = helper.getDeviceToTicks().get(actuator);
        //remove them from the array
        for (Integer tick : prevTicks)
        {
            refactoredPowerConsumption[tick] -= prop.getDeltaWhenWork() ;
        }

        for(List<Integer> ticks : subsets)
        {
            helper.updateConsumption(prop, ticks, refactoredPowerConsumption);
            double res = calculateTotalConsumptionWithPenalty(agent.getcSum(), refactoredPowerConsumption, agent.getCurrIteration().getPowerConsumptionPerTick()
                    ,helper.getNeighboursPriceConsumption(), agent.getAgentData().getPriceScheme());

            if (res >= agent.getTotalHousesPrice() && res >= bestPrice)
            {
                bestPrice = res;
                newTicks = ticks;
            }
        }
        return newTicks;
    }

    private double[] buildNewScheduleAccordingToNewTicks() {
        double[] powerConsumption = new double[FINAL_TICK+1];
        PropertyWithData prop = null;
        double delta = 0;
        for (Map.Entry<Actuator, List<Integer>> entry : helper.getDeviceToTicks().entrySet())
        {
            for (PropertyWithData p : helper.getAllProperties())
            {
                if (p.getActuator().getName().equals(entry.getKey().getName()))
                {
                    delta = p.getDeltaWhenWork();
                }
            }

            for(Integer tick : entry.getValue())
            {
                powerConsumption[tick] += delta;
            }
        }
        return powerConsumption;
    }

    public boolean buildScheduleFromScratch() {
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

        passiveRules.forEach(pRule -> helper.buildNewPropertyData(pRule, true));
        activeRules.forEach(pRule -> helper.buildNewPropertyData(pRule, false));

        helper.SetActuatorsAndSensors();
        double[] powerConsumption = tryBuildScheduleIterationZero();
        double price = calcPrice (powerConsumption);
        helper.totalPriceConsumption = price;
        agentIterationData = new AgentIterationData(currentNumberOfIter, agent.getName(),price, powerConsumption);
        agent.setCurrIteration(agentIterationData);

        //TODO: Update the best iteration.
        return true;
    }

    private double[] tryBuildScheduleIterationZero()
    {
        double[] powerConsumption = new double[FINAL_TICK+1];
        for(PropertyWithData prop : helper.getAllProperties().stream()
                .filter(p->p.isPassiveOnly()==false)
                .collect(Collectors.toList()))
        {

            double ticksToWork=helper.calcHowLongDeviceNeedToWork(prop);
            //draw ticks to work
            List<Integer> myTicks = new ArrayList<>();
            boolean flag = true;

            while (flag)
            {   //new iteration, flag starting with false as everything is okay.
                flag= false;
                int randomNum=0;
                for(int i=0; i<ticksToWork; ++i)
                {
                    switch (prop.getPrefix())
                    {
                        case BEFORE:    // Min + (int)(Math.random() * ((Max - Min) + 1))
                            randomNum = START_TICK + (int)(Math.random() * ((prop.getTargetTick() - START_TICK) + 1));
                            break;
                        case AFTER:
                            randomNum = (int) ((prop.getTargetTick()+1) + (int)(Math.random() * ((FINAL_TICK -  (prop.getTargetTick()+1)) + 1)));
                            break;
                        case AT:
                            randomNum = (int) prop.getTargetTick();
                            break;
                    }

                    if (!myTicks.contains(randomNum))
                    {
                        myTicks.add(randomNum);
                    }
                    else{
                        --i;
                    }
                }

                //there are sensors that reflect from this work! check if there is a problem with that.
                if (!prop.relatedSensorsDelta.isEmpty())
                {
                    for (String propName : prop.relatedSensorsDelta.keySet())
                    {
                        PropertyWithData relatedSensor = helper.getAllProperties().stream()
                                .filter(x->x.getName().equals(propName)).findFirst().get();
                        if (!relatedSensor.canBeModified(prop.relatedSensorsDelta.get(propName)))
                        {
                            //there is a problem with working at that hour, lets draw new tick.
                            flag = true;
                            break;
                        }
                    }
                }
            }

            helper.updateConsumption(prop, myTicks, powerConsumption);
        }

        return powerConsumption;
    }

    @Override
    public boolean done() {
        return finished;
    }

    public void setHelper(AlgorithmDataHelper helper) {
        this.helper = helper;
    }

    public AlgorithmDataHelper getHelper() {
        return helper;
    }

    @Override
    public DSA cloneBehaviour() {
        DSA newInstance = new DSA();
        newInstance.finished = this.finished;
        newInstance.currentNumberOfIter = this.currentNumberOfIter;
        newInstance.FINAL_TICK = this.FINAL_TICK;
        newInstance.agentIterationData = null;//will be created as part of the behaviour run(see buildScheduleFromScratch)
        return newInstance;
    }
}
