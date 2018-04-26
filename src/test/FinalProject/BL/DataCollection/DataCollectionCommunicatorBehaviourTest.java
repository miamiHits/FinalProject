package FinalProject.BL.DataCollection;

import FinalProject.BL.DataCollection.*;
import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class DataCollectionCommunicatorBehaviourTest {
    private DataCollectionCommunicatorBehaviour behaviour;
    private static final String SERVICE_TYPE = "dataCollectorTest";
    private static final String SERVICE_NAME = "DataCollectionCommunicatorTest";
    private static final String ONTOLOGY = "OntologyTest";

    @Before
    public void setUp() throws Exception {
        behaviour = new DataCollectionCommunicatorBehaviour();
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(new AID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(SERVICE_TYPE);
        sd.setName(SERVICE_NAME);
        sd.addOntologies(ONTOLOGY);
        dfd.addServices(sd);
        /*try {
            DFService.register(new Agent(), dfd);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }*/

    }

    @Test
    public void findAgents() throws Exception {
        //Assert.assertTrue(behaviour.findAgents(ONTOLOGY).length == 1);

    }

}