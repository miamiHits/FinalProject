package FinalProject;

import FinalProject.BL.Agents.DSA;
import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import org.apache.commons.io.output.CountingOutputStream;
import org.apache.log4j.Logger;

import java.io.*;
import java.lang.instrument.Instrumentation;
import java.util.Objects;

public class Utils {

    private final static Logger logger = Logger.getLogger(Utils.class);

    public static long getSizeOfObj(Object object){
        if (object == null) {
            logger.info("getSizeOfObj: obj is null! Returning 0");
            return 0;
        }

        try{
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            final CountingOutputStream out = new CountingOutputStream(outputStream);
            new ObjectOutputStream(out).writeObject(object);
            out.close();
            return out.getByteCount();
        }catch (IOException e){
            logger.info("Could not count size of object " + object.toString());
            return -1;
        }
    }

    public static String parseAgentName(AID aid)
    {
        String agentName = aid.getName();
        agentName = agentName.substring(0, agentName.indexOf('@'));
        return agentName;
    }

    public static String parseAgentName(Agent agent)
    {
        String agentName = agent.getName();
        agentName = agentName.substring(0, agentName.indexOf('@'));
        return agentName;
    }

    public static String parseSender(ACLMessage message)
    {
        return parseAgentName(message.getSender());
    }

}
