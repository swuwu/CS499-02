package swu.cs499.nmapforandroid;

/**
 * Created by swu on 5/1/17.
 */

public class Host {

    private String ip;
    private String name;

    public Host(String ip) {
        this.ip = ip;
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
}
