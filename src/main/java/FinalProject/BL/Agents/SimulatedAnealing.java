package FinalProject.BL.Agents;

import FinalProject.BL.IterationData.AgentIterationData;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

import static FinalProject.BL.DataCollection.PowerConsumptionUtils.calculateEPeak;

//TODO equals and hashCode
public class SimulatedAnealing extends SmartHomeAgentBehaviour{

    private final static Logger logger = Logger.getLogger(SimulatedAnealing.class);
    private Map<PropertyWithData, List<Set<Integer>>> propToSubsetsMap = new HashMap<>();
    private Map<PropertyWithData, Map<String,Integer>> propToSensorsToChargeMap = new HashMap<>();

    public SimulatedAnealing() { super(); }

    @Override
    protected void doIteration() {
        if (agent.isZEROIteration()) {
            buildScheduleFromScratch();
            agent.setZEROIteration(false);
            agent.setPriceSum(calcCsum(iterationPowerConsumption));
            beforeIterationIsDone();
        }
        else {
            helper.resetProperties();
            //only sets up for the build, prop by prop
            buildScheduleBasic(false); //randomize sched is ignored
            //do actual build for all devices at once (Roi asked for it to be this way)
            pickAndApplyRandomSched();
        }
        this.currentNumberOfIter++;
    }

    private void pickAndApplyRandomSched() {
        List<double[]> allScheds = agent.getMyNeighborsShed().stream()
                .map(AgentIterationData::getPowerConsumptionPerTick)
                .collect(Collectors.toList());

        Map<PropertyWithData, Set<Integer>> prevSchedForAllProps = new HashMap<>(propToSubsetsMap.size());
        propToSubsetsMap.keySet().forEach(prop -> {
            Set<Integer> prevTicks = new HashSet<>(getTicksForProp(prop));
            prevSchedForAllProps.put(prop, prevTicks);
        });
        double[] prevSched = helper.cloneArray(iterationPowerConsumption); //already with background load!
        prevSchedForAllProps.forEach((prop, ticks) -> {
            double powerCons = prop.getPowerConsumedInWork();
            ticks.forEach(tick -> prevSched[tick] += powerCons);
        });
        allScheds.add(prevSched);
        double prevGrade = calcImproveOptionGrade(prevSched, allScheds);

        allScheds.remove(prevSched);
        Map<PropertyWithData, Set<Integer>> randomSchedForAllProps = new HashMap<>(propToSubsetsMap.size());
        propToSubsetsMap.keySet().forEach(prop -> {
            Set<Integer> randSubset = pickRandomSubsetForProp(prop);
            randomSchedForAllProps.put(prop, randSubset);
        });
        double[] randSched = helper.cloneArray(iterationPowerConsumption); //already with background load!
        randomSchedForAllProps.forEach((prop, ticks) -> {
            double powerCons = prop.getPowerConsumedInWork();
            ticks.forEach(tick -> randSched[tick] += powerCons);
        });
        allScheds.add(randSched);
        double newGrade = calcImproveOptionGrade(randSched, allScheds);

        if (newGrade < prevGrade || shouldTakeNewSched()) {
            iterationPowerConsumption = randSched;
            randomSchedForAllProps.forEach((prop, ticks) ->
                    updateTotals(prop,new ArrayList<>(ticks), propToSensorsToChargeMap.get(prop)));
        }
        else {
            iterationPowerConsumption = prevSched;
            prevSchedForAllProps.forEach((prop, ticks) ->
                    updateTotals(prop,new ArrayList<>(ticks), propToSensorsToChargeMap.get(prop)));
        }
    }

    private boolean shouldTakeNewSched() {
        float probability = 1 - (currentNumberOfIter / agent.getAgentData().getNumOfIterations());
        return flipCoin(probability);
    }

    private Set<Integer> pickRandomSubsetForProp(PropertyWithData prop) {
        List<Set<Integer>> allSubsets = propToSubsetsMap.get(prop);
        //TODO: check what to do in case of zero subset
        int index = drawRandomNum(0, allSubsets.size() - 1);
        return allSubsets.get(index);
    }

    @Override
    protected void generateScheduleForProp(PropertyWithData prop, double ticksToWork, Map<String, Integer> sensorsToCharge, boolean randomSched) {
        if (agent.isZEROIteration()) {
            startWorkZERO(prop, sensorsToCharge, ticksToWork);
        }
        else {
//            startWorkNonZeroIter(prop, sensorsToCharge, ticksToWork, randomSched);
            if (!propToSubsetsMap.containsKey(prop)) {
                initSubsetsForProp(prop, ticksToWork);
            }
            addSensorsToChargeForPropIfAbsent(prop, sensorsToCharge);
        }
    }

    private void addSensorsToChargeForPropIfAbsent(PropertyWithData prop, Map<String, Integer> sensorsToCharge) {
        propToSensorsToChargeMap.putIfAbsent(prop,sensorsToCharge);
    }

    private List<Set<Integer>> initSubsetsForProp(PropertyWithData prop, double ticksToWork) {
        List<Set<Integer>> subsets;
        if (ticksToWork <= 0) {
            //TODO getting 0 sized subset here!
            subsets = checkAllSubsetOptions(prop);
            if (subsets == null ) {
                logger.warn("subsets is null!");
            }
        }
        else {
            List<Integer> rangeForWork = calcRangeOfWork(prop);
            subsets = helper.getSubsets(rangeForWork, (int) ticksToWork);
        }
        propToSubsetsMap.put(prop, subsets);
        return subsets;
    }

    @Override
    protected void onTermination() {
        logger.info(agent.getName() + " for problem " +
                agent.getProblemId() + "and algo SimulatedAnealing is TERMINATING!");
    }

    @Override
    protected void countIterationCommunication() {
        //TODO
    }

    @Override
    public SimulatedAnealing cloneBehaviour() {
        SimulatedAnealing newInstance = new SimulatedAnealing();
        newInstance.finished = this.finished;
        newInstance.currentNumberOfIter = this.currentNumberOfIter;
        newInstance.FINAL_TICK = this.FINAL_TICK;
        newInstance.agentIterationData = null; //will be created as part of the behaviour run(see buildScheduleFromScratch)
        return newInstance;
    }

    @Override
    protected double calcImproveOptionGrade(double[] newPowerConsumption, List<double[]> allScheds) {
        double price = calcCsum(newPowerConsumption);
        return price + calculateEPeak(allScheds);
    }
}
