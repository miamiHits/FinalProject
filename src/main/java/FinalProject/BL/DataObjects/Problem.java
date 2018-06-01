package FinalProject.BL.DataObjects;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Problem implements Serializable
{

    private String id;
    private Map<Integer, List<Device>> allDevices;
    private List<AgentData> allHomes;
    @SerializedName("horizon")
    private int horizon;
    @SerializedName("priceSchema")
    private double[] priceScheme;
    @SerializedName("granularity")
    private int granularity;

    public Problem(String id,Map<Integer, List<Device>> allDevices, List<AgentData> allHomes, int horizon, int granularity,
                   double[] priceScheme)
    {
        this.id = id;
        this.allDevices = allDevices;
        this.allHomes = allHomes;
        this.horizon = horizon;
        this.granularity = granularity;
        this.priceScheme = priceScheme;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public Map<Integer, List<Device>> getAllDevices()
    {
        return allDevices;
    }

    public void setAllDevices(Map<Integer, List<Device>> allDevices)
    {
        this.allDevices = allDevices;
    }

    public void setAllHomes(List<AgentData> allHomes)
    {
        this.allHomes = allHomes;
    }

    public int getHorizon()
    {
        return horizon;
    }

    public void setHorizon(int horizon)
    {
        this.horizon = horizon;
    }

    public int getGranularity()
    {
        return granularity;
    }

    public void setGranularity(int granularity)
    {
        this.granularity = granularity;
    }

    public double[] getPriceScheme()
    {
        return priceScheme;
    }

    public void setPriceScheme(double[] priceScheme)
    {
        this.priceScheme = priceScheme;
    }

    public List<AgentData> getAgentsData()
    {
        return this.allHomes;
    }

    @Override
    public String toString()
    {
        return "Problem{" +
                "id='" + id + '\'' +
                ", allDevices=" + allDevices +
                ", allHomes=" + allHomes +
                ", horizon=" + horizon +
                ", granularity=" + granularity +
                ", priceScheme=" + Arrays.toString(priceScheme) +
                '}';
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        Problem problem = (Problem) o;

        if (getHorizon() != problem.getHorizon())
        {
            return false;
        }
        if (getGranularity() != problem.getGranularity())
        {
            return false;
        }
        if (!getId().equals(problem.getId()))
        {
            return false;
        }
        for (int houseType : getAllDevices().keySet())
        {
            if (!problem.getAllDevices().get(houseType).equals(getAllDevices().get(houseType)))
            {
                return false;
            }
        }
        if (!getAgentsData().equals(getAgentsData()))
        {
            return false;
        }
        return Arrays.equals(getPriceScheme(), problem.getPriceScheme());

    }

    @Override
    public int hashCode()
    {
        int result = getId().hashCode();
        result = 31 * result + getAllDevices().hashCode();
        result = 31 * result + getAgentsData().hashCode();
        result = 31 * result + getHorizon();
        result = 31 * result + Arrays.hashCode(getPriceScheme());
        result = 31 * result + getGranularity();
        return result;
    }
}
