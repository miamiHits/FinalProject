package FinalProject.BL.Agents;

import FinalProject.BL.IterationData.AgentIterationData;
import FinalProject.Utils;
import jade.lang.acl.ACLMessage;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

import static FinalProject.BL.DataCollection.PowerConsumptionUtils.calculateEPeak;

/**
 * This class implements the SA (Simulated Anealing) algorithm and is provided
 * as is, as part of the system.
 *
 * The following is pseudo-code for the algorithm (including actions that are necessary for the work
 * of the system but are not part of the algorithm per say, such as sending messages to the data collector):
 *
 * init: (iteration 0):
 * generate a random schedule satisfying the constraints
 * send iteration data to collector
 * send schedule to all neighbours
 *
 * iterations (iteration > 0):
 * receive schedule from all neighbours
 * build tick subsets for all properties (if not built already)
 * pick a random subset for each property
 * if the new schedule improves the total grade (calculation including Csum and Epeak, see calcImproveOptionGrade):
 *      switch to new schedule
 * else:
 *      switch to the new schedule with probability 1 - (current number of iteration / total number of iterations)
 * send iteration data to collector
 * send schedule to all neighbours
 */
public class SA extends SmartHomeAgentBehaviour{

    private final static Logger logger = Logger.getLogger(SA.class);

    public SA() { super(); }

    /**
     * Overridden from {@link SmartHomeAgentBehaviour}.
     * This is the main methods that runs SA specific logic
     */
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

    /**
     * Wait for messages from all neighbours (sent in previous iteration),
     * read the messages into agent.getMyNeighborsShed,
     * calculate the power consumption for all neighbours
     */
    private void receiveAllMessagesAndHandleThem() {
        List<ACLMessage> messageList = waitForNeighbourMessages(SmartHomeAgent.MESSAGE_TEMPLATE_SENDER_IS_NEIGHBOUR);
        readNeighboursMsgs(messageList);
        helper.calcPowerConsumptionForAllNeighbours();
    }

    /**
     * Pick a random schedule for each property (out of the pre-calculated subsets),
     * calculate the total grade of this schedule,
     * compare to total grade of previous schedule.
     * Switch to the new schedule if it is better than previous schedule, or with probability (current number of iteration / total number of iterations)
     */
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

    /**
     * @return whether or not to switch to a new schedule
     * which is not better than the previous one (called from pickAndApplyRandomSched).
     */
    private boolean shouldTakeNewSched() {
        float probability = calcProbabilityToTakeNewSched();
        return flipCoin(probability);
    }

    /**
     * Calculate the probability to take a schedule which is not better than the previous one.
     * The probability is defined by: 1 - (current number of iteration / total number of iterations)
     * @return -1 if the current number of iteration is <= 0
     * else, 1 - (current number of iteration / total number of iterations).
     */
    private float calcProbabilityToTakeNewSched() {
        if (agent.getAgentData().getNumOfIterations() <= 0 || currentNumberOfIter <= 0) {
            logger.info("Num of iter <= 0 or currentNumberOfIter <= 0!");
            return -1;
        }

        return 1 - ((float) currentNumberOfIter / agent.getAgentData().getNumOfIterations());
    }

    /**
     * Pick a random subset of ticks to work in for property prop
     * out of all the possible subsets for it.
     * @param prop The {@link PropertyWithData} to which a random subset of ticks should be chosen.
     * @return A random set of ticks for {@link PropertyWithData} prop to work in.
     * The ticks satisfy all constraints for this property.
     */
    private Set<Integer> pickRandomSubsetForProp(PropertyWithData prop) {
        List<Set<Integer>> allSubsets = propToSubsetsMap.get(prop);
        if (allSubsets == null || allSubsets.isEmpty()) {
            return new HashSet<>(0);
        }
        int index = drawRandomNum(0, allSubsets.size() - 1);
        return allSubsets.get(index);
    }

    /**
     * Overridden from {@link SmartHomeAgentBehaviour}.
     * In this algorithm, this methods works a bit differently to other algorithms (see {@link DSA}, {@link SHMGM}, etc.).
     * If currentNumberOfIter == 0, build a schedule for {@link PropertyWithData} prop (same as other algorithms),
     * else, generate subsets for prop if not already generated.
     * @param prop the property to which the schedule should be generated
     * @param ticksToWork number of active ticks needed
     * @param sensorsToCharge sensors affected
     * @param randomSched ignored in this algorithm.
     */
    @Override
    protected void generateScheduleForProp(PropertyWithData prop, double ticksToWork, Map<String, Integer> sensorsToCharge, boolean randomSched) {
        if (agent.isZEROIteration()) {
            startWorkZERO(prop, sensorsToCharge, ticksToWork);
        }
        else {
            if (!propToSubsetsMap.containsKey(prop)) {
                getSubsetsForProp(prop, ticksToWork); //to put in map if absent
            }
        }
    }

    /**
     * Overridden from {@link SmartHomeAgentBehaviour}.
     * This method is called just before this agent is terminated (when the run has finished
     * for this (algorithm, problem) pair. In this algorithm, it only prints to log.
     */
    @Override
    protected void onTermination() {
        logger.info(agent.getName() + " for problem " +
                agent.getProblemId() + "and algo SA is TERMINATING!");
    }

    /**
     * Count the number and total size of messages sent by this agent
     * during this iteration. This is important for data collection
     * and not for the actual run of the algorithm.
     */
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

    /**
     * @return A deep copy of this instance.
     */
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
