package FinalProject;

import jade.core.Agent;
import org.apache.commons.io.output.CountingOutputStream;
import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

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

    public static String parseAgentName(Agent agent)
    {
        String agentName = agent.getName();
        return parseAgentName(agentName);
    }

    public static String parseAgentName(String agentName) {
        int shtrudel = agentName.indexOf('@');
        if (shtrudel != -1){
            agentName = agentName.substring(0, shtrudel);
        }
        return agentName;
    }

}
