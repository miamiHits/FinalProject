package FinalProject.BL.DataCollection;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class IterationAgentsPrice {
    private Map<Integer, List<AgentPrice>> iterationToAgentsPrice;

    public IterationAgentsPrice() {
        iterationToAgentsPrice = new HashMap<Integer, List<AgentPrice>>();
    }

    public List<AgentPrice> getAgentsPrices(int iterationNum){
        return iterationToAgentsPrice.get(iterationNum);
    }

    public boolean isIterationOver(int iterationNum, int numOfAgents){
        List<AgentPrice> agentsPrices = iterationToAgentsPrice.get(iterationNum);
        if (agentsPrices != null){return agentsPrices.size() == numOfAgents;}
        return false;
    }

    public void addAgentPrice(int iterationNum, AgentPrice agentPrice ){
        if (iterationToAgentsPrice.containsKey(iterationNum)){
            iterationToAgentsPrice.get(iterationNum).add(agentPrice);
        }
        else{
            List<AgentPrice> agentsPrices = new LinkedList<AgentPrice>();
            agentsPrices.add(agentPrice);
            iterationToAgentsPrice.put(iterationNum, agentsPrices);
        }
    }

    public Map<Integer, List<AgentPrice>> getIterationToAgentsPrice() {
        return iterationToAgentsPrice;
    }

    public void setIterationToAgentsPrice(Map<Integer, List<AgentPrice>> iterationToAgentsPrice) {
        this.iterationToAgentsPrice = iterationToAgentsPrice;
    }

}
