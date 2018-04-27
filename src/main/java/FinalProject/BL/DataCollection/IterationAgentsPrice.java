package FinalProject.BL.DataCollection;

import FinalProject.Utils;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

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

    public boolean isIterationOverNoEpeak(int iterationNum, int numOfAgents){
        List<AgentPrice> agentsPrices = iterationToAgentsPrice.get(iterationNum);
        if (agentsPrices != null){
            return agentsPrices.size() == numOfAgents; //&& epeakCalculated(iterationNum);
        }
        return false;
    }

    public boolean onlyfirstEpeakArrived(int iterNum){
        List<NeighborhoodEpeak> ne = iterationsToNeighborhoodsPeak.get(iterNum);
        if (ne == null){return false;}
        int count = 0;
        for (NeighborhoodEpeak e: ne){
            if (e.getCountEpeaks() > 1 ){
                return false;
            }
            else if (e.getCountEpeaks() == 1){
                count ++;
            }
        }
        return count == 1;
    }

    public boolean ePeakCalculated(int iterNum) {
        List<NeighborhoodEpeak> ne = iterationsToNeighborhoodsPeak.get(iterNum);
        if (ne == null){return false;}
        for (NeighborhoodEpeak e: ne){
            if (e.getEpeak() == -1 || !e.gotAllEpeaks()){
                return false;
            }
        }
        return true;
    }

    public void addAgentPrice(int iterationNum, AgentPrice agentPrice ){
        String name1 = agentPrice.getAgentName();
        String name2 = "";
        int shtrudel = name1.indexOf('@');
        if (shtrudel != -1){
            name1 = name1.substring(0, shtrudel);
        }

        if (iterationToAgentsPrice.containsKey(iterationNum)){
            List<AgentPrice> prices = iterationToAgentsPrice.get(iterationNum);
            for(AgentPrice ag : prices){
                name2 = ag.getAgentName();
                shtrudel = name2.indexOf('@');
                if (shtrudel != -1){
                    name2 = name2.substring(0, shtrudel);
                }
                if(name1.equals(name2)){
                    return;
                }
            }
            prices.add(agentPrice);
        }
        else{
            List<AgentPrice> agentsPrices = new LinkedList<AgentPrice>();
            agentsPrices.add(agentPrice);
            iterationToAgentsPrice.put(iterationNum, agentsPrices);
        }
    }

    public void addNeighborhoodAndEpeak(int iterationNum, double epeak, Set<String> neighborhood){
        List<NeighborhoodEpeak> neigEpeak = iterationsToNeighborhoodsPeak.get(iterationNum);
        boolean exist = false;
        if (neigEpeak == null){
            neigEpeak = new LinkedList<>();
            neigEpeak.add(new NeighborhoodEpeak(neighborhood, epeak));
            iterationsToNeighborhoodsPeak.put(iterationNum, neigEpeak);
        }
        else{
            for (NeighborhoodEpeak ne: neigEpeak) {
                if (ne.getNeighborhood().containsAll(neighborhood)){
                    if(epeak != -1 && epeak != ne.getEpeak()){
                        ne.setEpeak(epeak);
                    }
                    ne.addEpeak();
                    exist = true;
                    break;
                }
            }
            if (!exist){
                neigEpeak.add(new NeighborhoodEpeak(neighborhood, epeak));
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


    public double getTotalEpeakInIter(int iterNum) {
        double totalEpeak = 0;
        List<NeighborhoodEpeak> ne = iterationsToNeighborhoodsPeak.get(iterNum);
        for (NeighborhoodEpeak n : ne){
            if (n.getEpeak() == -1){
                logger.warn("getTotalEpeakInIter encountered -1 in epeak property");
                continue;
            }
            totalEpeak += n.getEpeak();
        }
        return totalEpeak;
    }

    public void calcEpeakForIter0() {
        List<AgentPrice> iterZeroAPs = iterationToAgentsPrice.get(0);
        List<NeighborhoodEpeak> iterZeroHoodEpeaks = iterationsToNeighborhoodsPeak.get(0);
        if (iterZeroAPs == null || iterZeroAPs.isEmpty() || iterZeroHoodEpeaks == null || iterZeroHoodEpeaks.isEmpty()) {
            logger.error("No AgentPrices or NeighborhoodEpeaks for iteration 0!");
            return;
        }
        iterZeroHoodEpeaks.forEach(hood -> {
            Set<String> agents = hood.getNeighborhood();
            List<AgentPrice> hoodAPs = iterZeroAPs.stream()
                    .filter(ap -> agents.contains(Utils.parseAgentName(ap.getAgentName())))
                    .collect(Collectors.toList());
            List<double[]> hoodScheds = hoodAPs.stream()
                    .map(AgentPrice::getSchedule)
                    .collect(Collectors.toList());

            double hoodEpeak = PowerConsumptionUtils.calculateEPeak(hoodScheds);
            hood.setEpeak(hoodEpeak);
            hood.setCountEpeaks(hood.getNeighborhood().size());
        });

    }
}
