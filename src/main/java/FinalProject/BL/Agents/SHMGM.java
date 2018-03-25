package FinalProject.BL.Agents;

import FinalProject.BL.IterationData.AgentIterationData;
import FinalProject.BL.IterationData.IterationCollectedData;
import FinalProject.Utils;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class SHMGM extends SmartHomeAgentBehaviour{

    private final static Logger logger = Logger.getLogger(SHMGM.class);
    private ImprovementMsg maxImprovementMsg = null; //used to calc msgs size only
    private final String gainMsgOntology = "GAIN_MSG";
    private MessageTemplate improvementTemplate = MessageTemplate.MatchOntology(gainMsgOntology);

    @Override
    protected void doIteration() {
        if (agent.isZEROIteration()) {
            logger.info("Starting work on Iteration: 0");
            buildScheduleFromScratch();
            agent.setZEROIteration(false);
            agent.setPriceSum(calcPrice(iterationPowerConsumption));
            beforeIterationIsDone();
        }
        else {
            logger.info("Starting work on Iteration: " + currentNumberOfIter);
            List<ACLMessage> messageList = waitForNeighbourMessages(SmartHomeAgent.MESSAGE_TEMPLATE_SENDER_IS_NEIGHBOUR);
            readNeighboursMsgs(messageList);
            helper.calcPowerConsumptionForAllNeighbours(); //TODO added
            improveSchedule();
        }
//        beforeIterationIsDone(); //TODO check if its good
        this.currentNumberOfIter++;
    }

    private void improveSchedule() {
        //backup prev values
        //TODO: test backup well!

        System.out.println(agent.getLocalName() + "'s iter " + currentNumberOfIter + " sched BEFORE is: " + Arrays.toString(iterationPowerConsumption) + " $$$ " + Arrays.toString(agent.getCurrIteration().getPowerConsumptionPerTick()));

        AlgorithmDataHelper helperBackup = new AlgorithmDataHelper(helper);
        double[] prevIterPowerConsumption = helper.cloneArray(iterationPowerConsumption);
        AgentIterationData prevIterData = new AgentIterationData(agentIterationData);
        AgentIterationData prevCurrIterData = new AgentIterationData(agent.getCurrIteration());
        IterationCollectedData prevCollectedData = new IterationCollectedData(agentIterationCollected);
        double oldPrice = calcPrice(prevIterPowerConsumption);
        double prevTotalCost = helper.calcTotalPowerConsumption(oldPrice); //also sets helper's epeak
        double oldEpeak = helper.ePeak;
        double prevAgentPriceSum = agent.getPriceSum();
        agent.setPriceSum(oldPrice);

        helper.resetProperties();
        buildScheduleBasic(); //using Ci as priceSum

        //calculate improvement
        double newPrice = calcPrice(iterationPowerConsumption); //iterationPowerConsumption changed by buildScheduleBasic
        double newTotalCost = helper.calcTotalPowerConsumption(newPrice, iterationPowerConsumption);

        double improvement =  prevTotalCost - newTotalCost;
        System.out.println(agent.getLocalName() + "'s iter " + currentNumberOfIter + " prevTotalCost: " + prevTotalCost + ", newTotalCost: " + newTotalCost + ", oldPrice: " + oldPrice +", newPrice: " + newPrice + ", impro: " + improvement);

        ImprovementMsg impMsg = sendImprovementToNeighbours(improvement);
        List<ImprovementMsg> receivedImprovements = receiveImprovements();
        receivedImprovements.add(impMsg);
        ImprovementMsg max = receivedImprovements.stream().max(ImprovementMsg::compareTo).orElse(null);
        maxImprovementMsg = max;
        if (max == null) {
            logger.error("max is null! Something went wrong!!!!!!!");
            //TODO: maybe use oldPrice instead of prevAgentPriceSum
            resetToPrevIterationData(helperBackup, prevIterData, prevCollectedData, prevCurrIterData, prevAgentPriceSum, prevTotalCost, prevIterPowerConsumption);
            return;
        }

        String agentName = beautifyAgentName(agent.getName());
        String maxName = beautifyAgentName(max.getAgentName());
        logger.info("agent is: " + agentName +" max is: " + maxName + " iter num: " + this.currentNumberOfIter);

        if (maxName.equals(agentName)) { //take new schedule
            logger.info(agent.getName() + "'s improvement: " + max.getImprovement() + " WAS THE GREATEST");
            agent.setPriceSum(newPrice);
            helper.totalPriceConsumption = newTotalCost;
            beforeIterationIsDone(); //TODO check if its good
        }
        else { //take prev schedule
            logger.info(agent.getName() + " got max improvement: " + max.getImprovement() + " from agent " + max.getAgentName());
            //TODO: maybe use oldPrice instead of prevAgentPriceSum
            helper.ePeak = oldEpeak;
            resetToPrevIterationData(helperBackup, prevIterData, prevCollectedData, prevCurrIterData, prevAgentPriceSum, prevTotalCost, prevIterPowerConsumption);
            agentIterationCollected.setIterNum(currentNumberOfIter);
            agentIterationCollected.setePeak(oldEpeak);
        }

        System.out.println(agent.getLocalName() + "'s iter " + currentNumberOfIter + " sched AFTER is: " + Arrays.toString(iterationPowerConsumption) + " $$$ " + Arrays.toString(agent.getCurrIteration().getPowerConsumptionPerTick()));


    }


    private String beautifyAgentName(String name) {
        int shtrudel = name.indexOf('@');
        if (shtrudel != -1){
            name = name.substring(0, shtrudel);
        }
        return name;
    }

    //TODO: test this well!
    private void resetToPrevIterationData(AlgorithmDataHelper helperBackup, AgentIterationData prevIterData, IterationCollectedData prevCollectedData,
                                          AgentIterationData prevCurrIterData, double prevPriceSum,
                                          double prevTotalCost, double[] prevIterPowerConsumption) {
        this.helper = helperBackup;
        this.agentIterationData = prevIterData;
        this.agentIterationCollected = prevCollectedData;
        this.agent.setCurrIteration(prevCurrIterData);
        agent.setPriceSum(prevPriceSum);
        helper.totalPriceConsumption = prevTotalCost;
        this.iterationPowerConsumption = prevIterPowerConsumption;
    }

    private List<ImprovementMsg> receiveImprovements() {

        List<ACLMessage> receivedMsgs = waitForNeighbourMessages(improvementTemplate);
        List<ImprovementMsg> improvements = receivedMsgs.stream()
                .map(msg -> {
                    try {
                        return (ImprovementMsg) msg.getContentObject();
                    } catch (UnreadableException e) {
                        logger.error("Could not read improvement msg: " + msg);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        logger.info(agent.getName() + " got improvement to neighbours");
        return improvements;
    }

    private ImprovementMsg sendImprovementToNeighbours(double improvement) {
        logger.info(agent.getName() + " sending improvement to neighbours");
        ImprovementMsg improvementToSend = new ImprovementMsg(agent.getName(), improvement, agent.getIterationNum());
        sendMsgToAllNeighbors(improvementToSend, gainMsgOntology);
        return improvementToSend;
    }

    @Override
    protected void onTermination() {
        logger.info(agent.getName() + " for problem " + agent.getProblemId() + "and algo SH-MGM is TERMINATING!");
    }

    @Override
    protected void countIterationCommunication() {
        int count = 1;

        //calc data sent to neighbours
        long totalSize = 0;
        long iterationDataSize = Utils.getSizeOfObj(agentIterationData);
        int neighboursSize = agent.getAgentData().getNeighbors().size();
        iterationDataSize *= neighboursSize;
        totalSize += iterationDataSize;
        count += neighboursSize;

        if (currentNumberOfIter > 0) {
            long improvementMsgSize = Utils.getSizeOfObj(maxImprovementMsg);
            improvementMsgSize *= neighboursSize;
            totalSize += improvementMsgSize;
            count += neighboursSize;
        }

        //calc messages to devices:
        final int constantNumOfMsgs = currentNumberOfIter == 0 ? 3 : 2;
        addMessagesSentToDevicesAndSetInAgent(count, totalSize, constantNumOfMsgs);
    }

    @Override
    protected void generateScheduleForProp(PropertyWithData prop, double ticksToWork, Map<String, Double> sensorsToCharge) {
        if (agent.isZEROIteration()) {
            startWorkZERO(prop, sensorsToCharge, ticksToWork);
        }
        else {
            startWorkNonZeroIter(prop, sensorsToCharge, ticksToWork);
        }
    }

    @Override
    public SmartHomeAgentBehaviour cloneBehaviour() {
        SHMGM newInstance = new SHMGM();
        newInstance.finished = this.finished;
        newInstance.currentNumberOfIter = this.currentNumberOfIter;
        newInstance.FINAL_TICK = this.FINAL_TICK;
        newInstance.agentIterationData = null; //will be created as part of the behaviour run(see buildScheduleFromScratch)
        return newInstance;
    }

}
