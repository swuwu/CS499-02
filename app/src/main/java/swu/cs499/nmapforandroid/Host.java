package swu.cs499.nmapforandroid;

import java.util.ArrayList;

/**
 * Created by swu on 5/1/17.
 */

public class Host
{
    private String ip;
    private ArrayList<Port> portList;

    public Host(String ip, ArrayList<Port> portList)
    {
        this.ip = ip;
        this.portList = portList;
    }

    public Host(String ip)
    {
        this.ip = ip;
        portList = new ArrayList<>();
    }

    public String getIp()
    {
        return ip;
    }

    public ArrayList<Port> getPortList()
    {
        return portList;
    }
}
