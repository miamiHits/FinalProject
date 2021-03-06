package FinalProject.BL.DataObjects;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Device implements Serializable
{

    protected String name;
    @SerializedName("subtype")
    protected String subtype;
    @SerializedName("location")
    protected String location;

    public Device(String name, String subtype, String location)
    {
        this.name = name;
        this.subtype = subtype;
        this.location = location;
    }

    public Device(Device other) {
        this.name = other.getName();
        this.subtype = other.getSubtype();
        this.location = other.getLocation();
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getSubtype()
    {
        return subtype;
    }

    public void setSubtype(String subtype)
    {
        this.subtype = subtype;
    }

    public String getLocation()
    {
        return location;
    }

    public void setLocation(String location)
    {
        this.location = location;
    }
}
