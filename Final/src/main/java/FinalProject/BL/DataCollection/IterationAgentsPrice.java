package FinalProject.BL.DataCollection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IterationAgentsPrice {
    private Map<Integer, List<AgentPrice>> iterationToAgentsPrice;

    public IterationAgentsPrice() {
        iterationToAgentsPrice = new HashMap<Integer, List<AgentPrice>>();
    }
}
