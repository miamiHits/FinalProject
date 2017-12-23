package FinalProject.BL.DataCollection;

import org.junit.Assert;
import org.junit.Test;


public class AgentPriceTest {

    @Test
    public void equals(){
        AgentPrice a1 = new AgentPrice("a1", 22.33);
        AgentPrice a2 = new AgentPrice("a2", 22.33);
        AgentPrice a3 = new AgentPrice("a1", 22.331);
        AgentPrice a4 = new AgentPrice("a1", 22.33);

        Assert.assertFalse(a1.equals(a2));
        Assert.assertFalse(a1.equals(a3));
        Assert.assertFalse(a1.equals(null));
        Assert.assertTrue(a1.equals(a4));
        Assert.assertTrue(a1.equals(a1));
    }

}