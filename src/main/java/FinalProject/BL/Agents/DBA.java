package FinalProject.BL.Agents;

import FinalProject.BL.DataObjects.Action;
import FinalProject.BL.DataObjects.Actuator;
import FinalProject.BL.IterationData.AgentIterationData;
import FinalProject.BL.IterationData.IterationCollectedData;
import jade.lang.acl.MessageTemplate;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

import static FinalProject.BL.DataCollection.PowerConsumptionUtils.calculateEPeak;

public class DBA extends SmartHomeAgentBehaviour{

    private final static Logger logger = Logger.getLogger(DBA.class);
    private final String gainMsgOntology = "GAIN_MSG";
    private MessageTemplate improvementTemplate;
    /* will tell about the "bags" the agent will have on specific ticks
       according to DBA logic
    */
    private int[] ticksBag = new int [iterationPowerConsumption.length];

    public DBA() {
        super();
        for(int i=0; i<ticksBag.length; i++)
            ticksBag[i] = 1;
    }


    @Override
    protected void doIteration() {
        if (agent.isZEROIteration()) {
            buildScheduleFromScratch();
            initMsgTemplate(); // needs to be here to make sure SmartHomeAgent class is init
            agent.setZEROIteration(false);
            agent.setPriceSum(calcCsum(iterationPowerConsumption));
            beforeIterationIsDone();
        }
        else {
            receiveNeighboursIterDataAndHandleIt();
            improveSchedule();
        }
        this.currentNumberOfIter++;
    }


    /**
     * Main logic of DBA algo.
     * Calculate the best option for a schedule based on
     * neighbours schedule received, send the improvement to
     * all neighbours and receive theirs.
     * If ALL the agents got 0 in their improvement , and in
     * 0.6 probabilty , the agent will add "bag" that will start in 1
     * to specific ticks. Will be done per agent.
     *
     */
    private void improveSchedule() {
        //backup prev iter's data
        AlgorithmDataHelper helperBackup = new AlgorithmDataHelper(helper);
        double[] prevIterPowerConsumption = helper.cloneArray(iterationPowerConsumption); //equals to agent.getCurrIteration().powerConsumptionPerTick
        AgentIterationData prevIterData = new AgentIterationData(agentIterationData);
        AgentIterationData prevCurrIterData = new AgentIterationData(agent.getCurrIteration());
        IterationCollectedData prevCollectedData = new IterationCollectedData(agentIterationCollected);
        double oldPrice = calcCsum(prevIterPowerConsumption);
        double prevTotalCost = helper.calcTotalPowerConsumption(oldPrice, iterationPowerConsumption); //also sets helper's epeak
        helper.totalPriceConsumption = prevTotalCost;
        double prevAgentPriceSum = agent.getPriceSum();
        agent.setPriceSum(oldPrice);

        //calc try to improve sched
        helper.resetProperties();

        buildScheduleBasic(false);
        //calculate improvement
        double newPrice = calcCsum(iterationPowerConsumption); //iterationPowerConsumption changed by buildScheduleBasic
        double actualEpeak = tempBestPriceConsumption - newPrice;

        boolean allDidntImproved = true;

        double improvement = prevTotalCost - tempBestPriceConsumption;
        ImprovementMsg impMsg = sendImprovementToNeighbours(improvement, prevIterPowerConsumption);
        List<ImprovementMsg> receivedImprovements = receiveImprovementMsgs();
        receivedImprovements.add(impMsg);
        ImprovementMsg max = receivedImprovements.stream().max(ImprovementMsg::compareTo).orElse(null);
        maxImprovementMsg = max;

        if (max == null) {
            logger.error("max is null! Something went wrong!!!!!!!");
            resetToPrevIterationData(helperBackup, prevIterData, prevCollectedData, prevCurrIterData,
                    prevAgentPriceSum, prevIterPowerConsumption, null, null);
            return;
        }

        if (improvement == 0.0)
        {
            //check if all agents didn't improve --> 0
            for(int i=0; i<receivedImprovements.size(); i++)
            {
                if (receivedImprovements.get(i).getImprovement() != 0.0)
                {
                    allDidntImproved = false;
                    break;
                }
            }
        }

        //not all agents got 0 in their improvements, we continue like regular iteration.
        if(!allDidntImproved)
        {
            //take new schedule
             takeNewSched(newPrice, actualEpeak);
        }
        //the "heart" of DBA algorithem will start to work
        else {
            //first need to raffle if this agent will build new sched with the bags.
            float PROBABILITY = 0.6f;
            int maxBag = checkMaxBagValue();
            if (flipCoin(PROBABILITY)) {
                // add bags
                for (Map.Entry<Actuator, Map<Action, List<Integer>>> entry: helperBackup.getDeviceToTicks().entrySet())
                {
                    for (Map.Entry<Action, List<Integer>> innerEntry : entry.getValue().entrySet())
                    {
                        for (int tick: innerEntry.getValue())
                        {
                            if (ticksBag[tick] < maxBag)
                            {
                                ticksBag[tick]++;
                            }
                        }
                    }
                }

                buildScheduleBasic(false);
                //build new sched according to new bags
                newPrice = calcCsum(iterationPowerConsumption); //iterationPowerConsumption changed by buildScheduleBasic
                actualEpeak = tempBestPriceConsumption - newPrice;
                takeNewSched(newPrice, actualEpeak);

            }
            else{
                //take prev schedule
                resetToPrevIterationData(helperBackup, prevIterData, prevCollectedData, prevCurrIterData,
                        prevAgentPriceSum, prevIterPowerConsumption, max.getImprevedSched(), max.getPrevSched());
            }

        }


    }

    private int checkMaxBagValue() {
        int max = Integer.MIN_VALUE;
        for(int i=0; i<ticksBag.length; i++)
        {
            max = Math.max(max, ticksBag[i]);
        }
        return max;
    }

    private void takeNewSched(double newPrice, double actualEpeak) {
        agent.setPriceSum(newPrice);
        helper.totalPriceConsumption = tempBestPriceConsumption;
        helper.ePeak = actualEpeak;
        beforeIterationIsDone();
    }

    @Override
    protected List<Integer> calcBestPrice(PropertyWithData prop, List<Set<Integer>> subsets) {
        List<Integer> newTicks = new ArrayList<>();
        double [] newPowerConsumption = helper.cloneArray(agent.getCurrIteration().getPowerConsumptionPerTick());
        List<double[]> allScheds = agent.getMyNeighborsShed().stream()
                .map(AgentIterationData::getPowerConsumptionPerTick)
                .collect(Collectors.toList());
        int index = allScheds.size();
        List<Integer> prevTicks = getTicksForProp(prop);
        if (prevTicks == null) {
            logger.error("calcBestPrice: prevTicks is null!");
            return null;
        }
        //remove them from the array
        for (Integer tick : prevTicks) {
            newPowerConsumption[tick] -= prop.getPowerConsumedInWork();
        }
        double[] copyOfNew = helper.cloneArray(newPowerConsumption);

        boolean improved = false;
        //find the best option
        for(Set<Integer> ticks : subsets) {
            //Adding the ticks to array
            for (Integer tick : ticks) {
                newPowerConsumption[tick] += prop.getPowerConsumedInWork();
            }
            allScheds.add(newPowerConsumption);

            //add the bags
            for (Integer tick : prevTicks) {
                newPowerConsumption[tick] = newPowerConsumption[tick] * this.ticksBag[tick];
            }

            double res = calcImproveOptionGrade(newPowerConsumption, allScheds);

            if (res <= helper.totalPriceConsumption && res <= tempBestPriceConsumption) {
                tempBestPriceConsumption = res;
                newTicks.clear();
                newTicks.addAll(ticks);
                improved = true;
            }

            //reset
            newPowerConsumption = helper.cloneArray(copyOfNew);
            allScheds.remove(index); //remove this new sched
        }

        if(!improved) {
            newTicks = getTicksForProp(prop);
        }

        return newTicks;
    }

    @Override
    protected void onTermination() {
        logger.info(agent.getName() + " for problem " + agent.getProblemId() + "and algo SH-MGM is TERMINATING!");
    }


    @Override
    protected void generateScheduleForProp(PropertyWithData prop, double ticksToWork, Map<String, Integer> sensorsToCharge, boolean randomSched) {
        if (agent.isZEROIteration()) {
            startWorkZERO(prop, sensorsToCharge, ticksToWork);
        }
        else {
            startWorkNonZeroIter(prop, sensorsToCharge, ticksToWork, randomSched);
        }
    }

    @Override
    public DBA cloneBehaviour() {
        DBA newInstance = new DBA();
        newInstance.finished = this.finished;
        newInstance.currentNumberOfIter = this.currentNumberOfIter;
        newInstance.FINAL_TICK = this.FINAL_TICK;
        newInstance.agentIterationData = null;
        return newInstance;
    }

    @Override
    protected double calcImproveOptionGrade(double[] newPowerConsumption, List<double[]> allScheds) {
        double price = calcCsum(newPowerConsumption);
        return price + calculateEPeak(allScheds);

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DBA shmgm = (DBA) o;

        boolean superEquals = super.equals(shmgm);
        return superEquals && (maxImprovementMsg == null && shmgm.maxImprovementMsg == null) ||
                (maxImprovementMsg != null && shmgm.maxImprovementMsg != null &&
                        maxImprovementMsg.equals(shmgm.maxImprovementMsg));
    }

    @Override
    public int hashCode() {
        return Objects.hash(maxImprovementMsg, gainMsgOntology, improvementTemplate);
    }
}
