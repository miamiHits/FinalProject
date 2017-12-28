package FinalProject.BL.Agents;
import FinalProject.BL.Experiment;
import FinalProject.BL.IterationData.AgentIterationData;
import FinalProject.BL.IterationData.IterationCollectedData;
import FinalProject.BL.Problems.*;
import FinalProject.DAL.AlgorithmLoader;
import FinalProject.Utils;
import jade.lang.acl.ACLMessage;
import org.apache.log4j.Logger;
import java.util.*;
import java.util.stream.Collectors;

import static FinalProject.BL.DataCollection.PowerConsumptionUtils.calculateTotalConsumptionWithPenalty;

public class DSA extends SmartHomeAgentBehaviour {

    private boolean finished = false;
    public static final int START_TICK = 0;
    private final static Logger logger = Logger.getLogger(DSA.class);

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
            List<ACLMessage> messageList = waitForNeighbourMessages();
            parseMessages(messageList);
            helper.calcTotalPowerConsumption(agent.getcSum());

            tryBuildSchedule();
            beforeIterationIsDone();
        }
        this.currentNumberOfIter ++;
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
            helper.setPowerConsumption(powerConsumption);
        }
        else{

            return;
        }
    }

    private void beforeIterationIsDone()
    {
        addBackgroundLoadToPriceScheme(helper.getPowerConsumption());
        double price = calcPrice(helper.getPowerConsumption());
        agentIterationData = new AgentIterationData(currentNumberOfIter, agent.getName(),price, helper.getPowerConsumption());
        agent.setCurrIteration(agentIterationData);
        agentIteraionCollected = new IterationCollectedData(currentNumberOfIter, agent.getName(),price, helper.getPowerConsumption(), agent.getProblemId(), agent.getAlgoId());
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
            //TODO: fix here
            //helper.updateConsumption(prop, ticks, refactoredPowerConsumption);
            double res = calculateTotalConsumptionWithPenalty(agent.getcSum(), refactoredPowerConsumption, agent.getCurrIteration().getPowerConsumptionPerTick()
                    ,helper.getNeighboursPriceConsumption(), agent.getAgentData().getPriceScheme());

            if (res >= agent.getcSum() && res >= bestPrice)
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
        //classifying the rules by activeness, start creating the prop object
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
        helper.checkForPassiveRules();
        helper.SetActuatorsAndSensors();
        logger.info(agent.getAgentData().getName() + "Finished build my prop object, start work on my schedule");
        tryBuildScheduleIterationZero();
        beforeIterationIsDone();

        return true;
    }

    private void tryBuildScheduleIterationZero()
    {
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
                try
                {
                    if (!prop.relatedSensorsDelta.isEmpty())
                    {
                        for (String propName : prop.relatedSensorsDelta.keySet())
                        {
                            if (helper.getAllProperties().stream()
                                    .filter(x->x.getName().equals(propName)).findFirst()!= null)
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
                  }
                  catch (Exception e)
                  {
                      logger.warn(agent.getAgentData().getName() + "Try to look for the related sensros , but not found like this");
                  }

            }

            helper.updateConsumption(prop, myTicks);
        }

        logger.info(agent.getAgentData().getName() + "Finished build sched");

    }

    @Override
    public boolean done() {
        boolean agentFinishedExperiment = (this.currentNumberOfIter > Experiment.maximumIterations);
        if (agentFinishedExperiment)
        {
            logger.info(Utils.parseAgentName(this.agent) + " ended its final iteration");
            this.agent.doDelete();
        }
        return agentFinishedExperiment;
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
