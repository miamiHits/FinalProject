package FinalProject.BL.Agents;

import FinalProject.BL.IterationData.AgentIterationData;
import FinalProject.Utils;
import jade.lang.acl.ACLMessage;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

import static FinalProject.BL.DataCollection.PowerConsumptionUtils.calculateEPeak;

public class SA extends SmartHomeAgentBehaviour{

    private final static Logger logger = Logger.getLogger(SA.class);
    private Map<PropertyWithData, List<Set<Integer>>> propToSubsetsMap = new HashMap<>();
    private Map<PropertyWithData, Map<String,Integer>> propToSensorsToChargeMap = new HashMap<>();

    public SA() { super(); }

    @Override
    protected void doIteration() {
        if (agent.isZEROIteration()) {
            buildScheduleFromScratch();
            agent.setZEROIteration(false);
            agent.setPriceSum(calcCsum(iterationPowerConsumption));
        }
        else {
            helper.resetProperties();
            receiveAllMessagesAndHandleThem();
            //only sets up for the build, prop by prop
            buildScheduleBasic(false); //randomize sched is ignored
            //do actual build for all devices at once (Roi asked for it to be this way)
            pickAndApplyRandomSched();
        }
        beforeIterationIsDone();
        this.currentNumberOfIter++;
    }

    private void receiveAllMessagesAndHandleThem() {
        List<ACLMessage> messageList = waitForNeighbourMessages(SmartHomeAgent.MESSAGE_TEMPLATE_SENDER_IS_NEIGHBOUR);
        readNeighboursMsgs(messageList);
        helper.calcPowerConsumptionForAllNeighbours();
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
        if (agent.getLocalName().equals("h1")) {
            logger.info("H1 iter: " + currentNumberOfIter + ", prevGrade: " + prevGrade);
        }

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
        if (agent.getLocalName().equals("h1")) {
            logger.info("H1 iter: " + currentNumberOfIter + ", newGrade: " + newGrade);
        }

        if (newGrade < prevGrade || shouldTakeNewSched()) {
            helper.totalPriceConsumption = newGrade;
            helper.ePeak = calculateEPeak(allScheds);
            //TODO commented out because updateTotals adds the ticks to iterationPowerConsumption
//            iterationPowerConsumption = randSched;
            randomSchedForAllProps.forEach((prop, ticks) ->
                    updateTotals(prop,new ArrayList<>(ticks), propToSensorsToChargeMap.get(prop)));
        }
        else {
            helper.totalPriceConsumption = prevGrade;
            allScheds.remove(randSched);
            allScheds.add(prevSched);
            helper.ePeak = calculateEPeak(allScheds);
            //TODO commented out because updateTotals adds the ticks to iterationPowerConsumption
//            iterationPowerConsumption = prevSched;
            prevSchedForAllProps.forEach((prop, ticks) ->
                    updateTotals(prop,new ArrayList<>(ticks), propToSensorsToChargeMap.get(prop)));
        }
    }

    private boolean shouldTakeNewSched() {
        float probability = calcProbabilityToTakeNewSched();
        return flipCoin(probability);
    }

    private float calcProbabilityToTakeNewSched() {
        if (agent.getAgentData().getNumOfIterations() <= 0 || currentNumberOfIter <= 0) {
            logger.info("Num of iter <= 0 or currentNumberOfIter <= 0!");
            return -1;
        }

        return 1 - ((float) currentNumberOfIter / agent.getAgentData().getNumOfIterations());
    }

    private Set<Integer> pickRandomSubsetForProp(PropertyWithData prop) {
        List<Set<Integer>> allSubsets = propToSubsetsMap.get(prop);
        if (allSubsets.isEmpty()) {
            return new HashSet<>(0);
        }
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
                agent.getProblemId() + "and algo SA is TERMINATING!");
    }

    @Override
    protected void countIterationCommunication() {
        int count = 0;
        //calc data sent to neighbours
        long totalSize = Utils.getSizeOfObj(agentIterationData);
        int neighboursSize = agent.getAgentData().getNeighbors().size();
        count += neighboursSize;
        totalSize *= neighboursSize;

        //calc messages to devices:
        if (currentNumberOfIter == 0 || currentNumberOfIter == 1) {
            final int constantNumOfMsgs = currentNumberOfIter == 0 ? 3 : 2;
            addMessagesSentToDevicesAndSetInAgent(count + 1, totalSize, constantNumOfMsgs);
        }
        else {
            final int size = helper.getAllProperties().size();
            totalSize += size * MSG_TO_DEVICE_SIZE;
            count += size;

            agent.setIterationMessageCount(count);
            agent.setIterationMessageSize(totalSize);
        }
    }

    @Override
    public SA cloneBehaviour() {
        SA newInstance = new SA();
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

//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        if (!super.equals(o)) return false;
//        SA sa = (SA) o;
//        return Objects.equals(propToSubsetsMap, sa.propToSubsetsMap) &&
//                Objects.equals(propToSensorsToChargeMap, sa.propToSensorsToChargeMap);
//    }
//
//    @Override
//    public int hashCode() {
//        int hash = super.hashCode();
//        return Objects.hash(propToSubsetsMap, propToSensorsToChargeMap, hash);
//    }
}
