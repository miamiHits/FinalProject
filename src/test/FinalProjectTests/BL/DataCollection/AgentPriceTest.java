package FinalProjectTests.BL.DataCollection;

import FinalProject.BL.DataCollection.*;
import org.junit.Assert;
import org.junit.Test;


public class AgentPriceTest {

    @Test
    public void SamePriceDiffrentName(){
        double[] consumptionPerTick = {22,2 ,23.2};
        AgentPrice a1 = new AgentPrice("a1", 22.33, consumptionPerTick, 1245);
        AgentPrice a2 = new AgentPrice("a2", 22.33, consumptionPerTick, 1245);
        Assert.assertFalse(a1.equals(a2));
    }

    @Test
    public void SameNameDIffrentPrice(){
        double[] consumptionPerTick = {22,2 ,23.2};
        AgentPrice a1 = new AgentPrice("a1", 22.33, consumptionPerTick, 1245);
        AgentPrice a3 = new AgentPrice("a1", 22.331, consumptionPerTick, 1245);
        Assert.assertFalse(a1.equals(a3));
    }

    @Test
    public void equalsWithNull(){
        double[] consumptionPerTick = {22,2 ,23.2};
        AgentPrice a1 = new AgentPrice("a1", 22.33, consumptionPerTick, 1245);
        Assert.assertFalse(a1.equals(null));
    }

    @Test
    public void SameNameSamePrice(){
        double[] consumptionPerTick = {22,2 ,23.2};
        AgentPrice a1 = new AgentPrice("a1", 22.33, consumptionPerTick, 1245);
        AgentPrice a4 = new AgentPrice("a1", 22.33, consumptionPerTick, 1245);
        Assert.assertTrue(a1.equals(a4));
    }

    @Test
    public void SameAgentPrice(){
        double[] consumptionPerTick = {22,2 ,23.2};
        AgentPrice a1 = new AgentPrice("a1", 22.33, consumptionPerTick, 1245);
        Assert.assertTrue(a1.equals(a1));
    }
}