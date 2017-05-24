package swu.cs499.nmapforandroid;

import java.util.ArrayList;

/**
 * Created by swu on 5/1/17.
 */

public class Host {

    private String ip;
    private String name;
    private ArrayList<Port> ports = new ArrayList<Port>();

    public Host(String ip, ArrayList<Port> p) {
        this.ip = ip;
        ports = p;
    }

    public String getIP() {
        return ip;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public ArrayList<Port> getPorts() {
        return ports;
    }
}
