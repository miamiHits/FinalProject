package FinalProject.BL.Agents;

import jade.lang.acl.ACLMessage;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SHMGM extends SmartHomeAgentBehaviour{

    private final static Logger logger = Logger.getLogger(SHMGM.class);

    @Override
    protected void doIteration() {
        if (agent.isZEROIteration()) {
            logger.info("Starting work on Iteration: 0");
            buildScheduleFromScratch();
            agent.setZEROIteration(false);
            logger.info("FINISH ITER 0");
        }
        else {
            List<ACLMessage> messageList = waitForNeighbourMessages();
            readNeighboursMsgs(messageList);
            helper.calcTotalPowerConsumption(agent.getcSum());
        }
        beforeIterationIsDone(); //TODO check if its good
        this.currentNumberOfIter++;
    }

    private void improveSchedule() {
        //backup prev values
        //TODO: test backup well!
        System.out.println("^^^^^^^^^^^^^^" + agent.getName() + Arrays.toString(iterationPowerConsumption));

        AlgorithmDataHelper helperBackup = new AlgorithmDataHelper(helper);
        double[] prevIterPowerConsumption = helper.cloneArray(iterationPowerConsumption);
        AgentIterationData prevIterData = new AgentIterationData(agentIterationData);
        AgentIterationData prevCurrIterData = new AgentIterationData(agent.getCurrIteration());
        IterationCollectedData prevCollectedData = new IterationCollectedData(agentIterationCollected);
        double oldPrice = calcPrice(prevIterPowerConsumption);
        double oldEpeak = helper.ePeak;
        double prevTotalCost = helper.calcTotalPowerConsumption(oldPrice); //also sets helper's epeak
        double prevAgentPriceSum = agent.getPriceSum();
        agent.setPriceSum(oldPrice);

        helper.resetProperties();
        buildScheduleBasic(); //using Ci as priceSum

        //calculate improvement
        double newPrice = calcPrice(iterationPowerConsumption); //iterationPowerConsumption changed by buildScheduleBasic
        double newTotalCost = helper.calcTotalPowerConsumption(newPrice);
        double improvement =  prevTotalCost - newTotalCost;

        ImprovementMsg impMsg = sendImprovmentToNeighbours(improvement);
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
            System.out.println("^^^^^^!!^^^^^^" + agent.getName() + Arrays.toString(iterationPowerConsumption));
            beforeIterationIsDone(); //TODO check if its good
        }
        else { //take prev schedule
            logger.info(agent.getName() + " got max improvement: " + max.getImprovement() + " from agent " + max.getAgentName());
            //TODO: maybe use oldPrice instead of prevAgentPriceSum
            helper.ePeak = oldEpeak;
            resetToPrevIterationData(helperBackup, prevIterData, prevCollectedData, prevCurrIterData, prevAgentPriceSum, prevTotalCost, prevIterPowerConsumption);
            System.out.println("^^^^^^^^^^^^^^" + agent.getName() + Arrays.toString(iterationPowerConsumption));
        }

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

    private ImprovementMsg sendImprovmentToNeighbours(double improvement) {
        logger.info(agent.getName() + " sending improvement to neighbours");
        ImprovementMsg improvementToSend = new ImprovementMsg(agent.getName(), improvement, agent.getIterationNum());
        sendMsgToAllNeighbors(improvementToSend, gainMsgOntology);
        return improvementToSend;
    }

    @Override
    protected void onTermination() {

    }

    @Override
    protected void countIterationCommunication() {

    }

    @Override
    protected void generateScheduleForProp(PropertyWithData prop, double ticksToWork, Map<String, Double> sensorsToCharge) {

    }

    @Override
    public DSA cloneBehaviour() {
        DSA newInstance = new DSA();
        newInstance.finished = this.finished;
        newInstance.currentNumberOfIter = this.currentNumberOfIter;
        newInstance.FINAL_TICK = this.FINAL_TICK;
        newInstance.agentIterationData = null; //will be created as part of the behaviour run(see buildScheduleFromScratch)
        return newInstance;
    }
}
