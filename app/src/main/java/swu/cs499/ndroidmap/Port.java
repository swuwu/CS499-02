package swu.cs499.ndroidmap;

/**
 * Created by swu on 5/8/17.
 */

public class Port
{
    int num;
    String type;
    String state;
    String service;

    public Port(int num, String type, String state, String service)
    {
        this.num = num;
        this.type = type;
        this.state = state;
        this.service = service;
    }

    public int getNum()
    {
        return num;
    }

    public String getType()
    {
        return type;
    }

    public String getState()
    {
        return state;
    }

    public String getService()
    {
        return service;
    }
}
