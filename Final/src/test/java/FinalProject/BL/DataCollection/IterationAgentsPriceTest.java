package FinalProject.BL.DataCollection;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class IterationAgentsPriceTest {
    private IterationAgentsPrice IAP;

    @Before
    public void setUp() throws Exception {
        IAP = new IterationAgentsPrice();
        double[] consumptionPerTick = {22,2 ,23.2};
        List<AgentPrice> l0 = new LinkedList<AgentPrice>();
        List<AgentPrice> l1 = new LinkedList<AgentPrice>();
        l1.add(new AgentPrice("a1", 22.22, consumptionPerTick));
        List<AgentPrice> l2 = new LinkedList<AgentPrice>();
        l2.add(new AgentPrice("a1", 22.22, consumptionPerTick));
        l2.add(new AgentPrice("a2", 33.33, consumptionPerTick));
        List<AgentPrice> l3 = new LinkedList<AgentPrice>();
        Map<Integer, List<AgentPrice>> map = new HashMap<Integer, List<AgentPrice>>();
        map.put(0, l0);
        map.put(1, l1);
        map.put(2, l2);
        map.put(3, l3);
        IAP.setIterationToAgentsPrice(map);
    }

    @Test
    public void isIterationOver() {
        Assert.assertFalse(IAP.isIterationOver(0, 2));
        Assert.assertFalse(IAP.isIterationOver(1, 2));
        Assert.assertTrue(IAP.isIterationOver(2, 2));
    }

    @Test
    public void addAgentPrice() {
        double[] consumptionPerTick = {22,2 ,23.2};
        Assert.assertTrue(IAP.getAgentsPrices(3).size() == 0);
        IAP.addAgentPrice(3, new AgentPrice("a1", 333.455,
                consumptionPerTick));
        Assert.assertTrue(IAP.getAgentsPrices(3).size() == 1);
        IAP.addAgentPrice(3, new AgentPrice("a2", 333.455,
                consumptionPerTick));
        Assert.assertTrue(IAP.getAgentsPrices(3).size() == 2);
    }

}