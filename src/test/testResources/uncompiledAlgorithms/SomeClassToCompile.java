public class SomeClassToCompile{

    private int intField;
    private double doubleField;
    private SomeClassToCompile objField;

    public SomeClassToCompile()
    {

    }

    public SomeClassToCompile(int intField, double doubleField, SomeClassToCompile objField)
    {
        this.intField = intField;
        this.doubleField = doubleField;
        this.objField = objField;
    }

    public int getIntField()
    {
        return intField;
    }

    public void setIntField(int intField)
    {
        this.intField = intField;
    }

    public double getDoubleField()
    {
        return doubleField;
    }

    public void setDoubleField(double doubleField)
    {
        this.doubleField = doubleField;
    }

    public SomeClassToCompile getObjField()
    {
        return objField;
    }

    public void setObjField(SomeClassToCompile objField)
    {
        this.objField = objField;
    }
}