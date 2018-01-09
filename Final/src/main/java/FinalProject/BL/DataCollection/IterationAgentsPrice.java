package FinalProject.BL.DataCollection;

import org.apache.log4j.Logger;

import java.util.*;

public class IterationAgentsPrice {
    private Map<Integer, List<AgentPrice>> iterationToAgentsPrice;
    private Map<Integer,List<NeighborhoodEpeak>> iterationsToNeighborhoodsPeak;
    private static final Logger logger = Logger.getLogger(IterationAgentsPrice.class);

    public IterationAgentsPrice() {
        iterationToAgentsPrice = new HashMap<Integer, List<AgentPrice>>();
        iterationsToNeighborhoodsPeak = new HashMap<Integer,List<NeighborhoodEpeak>>();
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

    public void addEpeakToNeighborhood(int iterationNum, double epeak, Set<String> neighborhood){
        List<NeighborhoodEpeak> neigEpeak = iterationsToNeighborhoodsPeak.get(iterationNum);
        if (neigEpeak == null){
            neigEpeak = new  LinkedList<NeighborhoodEpeak>();
            neigEpeak.add(new NeighborhoodEpeak(neighborhood, epeak));
            iterationsToNeighborhoodsPeak.put(iterationNum, neigEpeak);
        }
        else{
            for (NeighborhoodEpeak ne: neigEpeak) {
                if (ne.getNeighborhood().containsAll(neighborhood)){
                    if(epeak != ne.getEpeak()){
                        logger.warn("got different Epeak on same neighborhood");
                        ne.setEpeak(epeak);
                    }
                    break;
                }
            }
        }
    }

    public Map<Integer, List<AgentPrice>> getIterationToAgentsPrice() {
        return iterationToAgentsPrice;
    }

    public void setIterationToAgentsPrice(Map<Integer, List<AgentPrice>> iterationToAgentsPrice) {
        this.iterationToAgentsPrice = iterationToAgentsPrice;
    }

    public Map<Integer, List<NeighborhoodEpeak>> getIterationsToNeighborhoodsPeak() {
        return iterationsToNeighborhoodsPeak;
    }

    public void setIterationsToNeighborhoodsPeak(Map<Integer, List<NeighborhoodEpeak>> iterationsToNeighborhoodsPeak) {
        this.iterationsToNeighborhoodsPeak = iterationsToNeighborhoodsPeak;
    }



}
