package FinalProject;

import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;

public class Utils {

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
