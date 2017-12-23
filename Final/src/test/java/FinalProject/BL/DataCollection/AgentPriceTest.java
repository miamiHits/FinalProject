package FinalProject.BL.DataCollection;

import org.junit.Assert;
import org.junit.Test;


public class AgentPriceTest {

    @Test
    public void equals(){
        double[] consumptionPerTick = {22,2 ,23.2};
        AgentPrice a1 = new AgentPrice("a1", 22.33, consumptionPerTick);
        AgentPrice a2 = new AgentPrice("a2", 22.33, consumptionPerTick);
        AgentPrice a3 = new AgentPrice("a1", 22.331, consumptionPerTick);
        AgentPrice a4 = new AgentPrice("a1", 22.33, consumptionPerTick);

        Assert.assertFalse(a1.equals(a2));
        Assert.assertFalse(a1.equals(a3));
        Assert.assertFalse(a1.equals(null));
        Assert.assertTrue(a1.equals(a4));
        Assert.assertTrue(a1.equals(a1));
    }

}