package FinalProject.BL.Agents;

import FinalProject.Utils;
import jade.lang.acl.ACLMessage;
import org.apache.log4j.Logger;

import java.util.*;

import static FinalProject.BL.DataCollection.PowerConsumptionUtils.calculateEPeak;

/**
 * This class implements the SA (Simulated Anealing) algorithm and is provided
 * as is, as part of the system.
 *
 * The following is pseudo-code for the algorithm (including actions that are necessary for the work
 * of the system but are not part of the algorithm per say, such as sending messages to the data collector):
 * <code>
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
 * </code>
 *
 *
 * Please bear in mind, as opposed to other algorithms supplied as part of this system,
 * SA calculates an option for a new schedule for <b>all {@link FinalProject.BL.DataObjects.Device}s at once</b>.
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
        //iteration 0: init and build a random schedule that satisfies all of the constraints
        if (agent.isZEROIteration()) {
            buildScheduleFromScratch();
            agent.setZEROIteration(false);
            agent.setPriceSum(calcCsum(iterationPowerConsumption));
        }
        //iterations > 0: SA specific logic (as described at the top of this class)
        else {
            //reset all of this agents PropertyWithData to their starting values
            helper.resetProperties();
            //receive the neighbours messages sent in previous iteration
            receiveAllMessagesAndHandleThem();
            //only sets up for the build, prop by prop
            buildScheduleBasic(false); //randomize sched is ignored
            //do actual build for all devices at once
            pickAndApplyRandomSched();
        }
        //fill fields in agentIterationData (sent to neighbours)
        // and agentIterationCollected (sent to collector)
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
        List<double[]> allScheds = getNeighbourScheds();

        //calculate previous iteration grade:
        //create a map holding the ticks worked in the previous iteration by each property
        Map<PropertyWithData, Set<Integer>> prevSchedForAllProps = new HashMap<>(propToSubsetsMap.size());
        propToSubsetsMap.keySet().forEach(prop -> {
            Set<Integer> prevTicks = new HashSet<>(getTicksForProp(prop));
            prevSchedForAllProps.put(prop, prevTicks);
        });
        //we need to copy the array because we need to use it for calculating the new sched as well
        double[] prevSched = helper.cloneArray(iterationPowerConsumption); //already with background load!
        prevSchedForAllProps.forEach((prop, ticks) -> {
            double powerCons = prop.getPowerConsumedInWork();
            ticks.forEach(tick -> prevSched[tick] += powerCons);
        });
        allScheds.add(prevSched);
        double prevGrade = calcImproveOptionGrade(prevSched, allScheds); //the grade for the previous iteration

        //calculate a new random schedule (similar to above):
        allScheds.remove(prevSched); //we want to use the same list later, clean it
        Map<PropertyWithData, Set<Integer>> randomSchedForAllProps = new HashMap<>(propToSubsetsMap.size());
        //for each property, pick a random set of ticks (satisfying all constraints)
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
        double newGrade = calcImproveOptionGrade(randSched, allScheds); //the grade for this iteration

        //decide which of the 2 schedules to pick:
        if (newGrade < prevGrade || shouldTakeNewSched()) { //pick the new schedule
            helper.totalPriceConsumption = newGrade;
            helper.ePeak = calculateEPeak(allScheds);
            randomSchedForAllProps.forEach((prop, ticks) ->
                    updateTotals(prop,new ArrayList<>(ticks), propToSensorsToChargeMap.get(prop)));
        }
        else { //pick the previous schedule
            helper.totalPriceConsumption = prevGrade;
            allScheds.remove(randSched);
            allScheds.add(prevSched);
            helper.ePeak = calculateEPeak(allScheds);
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
    protected void generateScheduleForProp(PropertyWithData prop, double ticksToWork,
                                           Map<String, Integer> sensorsToCharge, boolean randomSched) {
        //iteration 0, build a schedule for prop
        if (agent.isZEROIteration()) {
            startWorkZERO(prop, sensorsToCharge, ticksToWork);
        }
        //non-zero iteration, just fill propToSubsetsMap if not already filled.
        //the schedule for all of the properties together will be built later in pickAndApplyRandomSched
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
     * Overridden from {@link SmartHomeAgentBehaviour}.
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
        //iteration 0 and 1 are different in the num of messages sent to devices
        //because in iteration 0 we build a whole sched and in iteration 1 we init propToSubsetsMap
        if (currentNumberOfIter == 0 || currentNumberOfIter == 1) {
            final int constantNumOfMsgs = currentNumberOfIter == 0 ? 3 : 2;
            addMessagesSentToDevicesAndSetInAgent(count + 1, totalSize, constantNumOfMsgs);
        }
        //for each device we send only 1 message in iterations > 1
        else {
            final int size = helper.getAllProperties().size();
            totalSize += size * MSG_TO_DEVICE_SIZE;
            count += size;

            agent.setIterationMessageCount(count);
            agent.setIterationMessageSize(totalSize);
        }
    }

    /**
     * Overridden from {@link SmartHomeAgentBehaviour}.
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

    /**
     * Overridden from {@link SmartHomeAgentBehaviour}.
     * The method used to calculate the grade of a given schedule.
     * The calculation in this algorithm (and in DSA, DBA and SHMGM) is:
     * ac * Csum + ae * Epeak.
     * @param newPowerConsumption the option for a schedule
     * @param allScheds a list of all schedules of neighbors and this agent
     * @return The total grade of this iteration.
     */
    @Override
    protected double calcImproveOptionGrade(double[] newPowerConsumption, List<double[]> allScheds) {
        double price = calcCsum(newPowerConsumption); //takes into account ac
        return price + calculateEPeak(allScheds); //takes into account ae
    }
}
