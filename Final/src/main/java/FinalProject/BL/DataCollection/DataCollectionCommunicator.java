package FinalProject.BL.DataCollection;

import FinalProject.BL.Experiment;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import org.apache.log4j.Logger;

import java.util.Map;

public class DataCollectionCommunicator extends Agent {

    public static final String SERVICE_TYPE = "dataCollector";
    public static final String SERVICE_NAME = "DataCollectionCommunicator";
    private Map<String, Integer> numOfAgentsInProblems;
    private Map<String, double[]> prices;
    private Experiment experiment;
    private static final Logger logger = Logger.getLogger(DataCollectionCommunicator.class);
    private DataCollector collector;

    public DataCollectionCommunicator() {
    }

    public DataCollectionCommunicator(Map<String, Integer> numOfAgentsInProblems, Map<String, double[]> prices) {
        collector = new DataCollector(numOfAgentsInProblems, prices);
    }

    @Override
    protected void setup() {
        logger.info("starting communicator");
        //add the cyclic behaviour
        addBehaviour(new DataCollectionCommunicatorBehaviour());
        //get args from builder
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            numOfAgentsInProblems = (Map<String, Integer>) args[0];
            prices = (Map<String, double[]>) args[1];
            experiment = (Experiment) args[2];
            collector = new DataCollector(numOfAgentsInProblems, prices);
        }
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(SERVICE_TYPE);
        sd.setName(SERVICE_NAME);
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
            logger.error("Communicator failed to register: " + fe);
        }
    }

    protected void takeDown() {
        // Deregister from the yellow pages
        try {
            DFService.deregister(this);
        }
        catch (FIPAException fe) {
            logger.error("DataCollectionCommunicator failed to terminate: " + fe);
        }
        // Printout a dismissal message
        logger.info("DataCollectionCommunicator "+getAID().getName()+" terminating.");
    }
    public DataCollector getCollector() {
        return collector;
    }

    public void setCollector(DataCollector collector) {
        this.collector = collector;
    }

    public Experiment getExperiment() {
        return experiment;
    }

    public void setExperiment(Experiment experiment) {
        this.experiment = experiment;
    }





}
