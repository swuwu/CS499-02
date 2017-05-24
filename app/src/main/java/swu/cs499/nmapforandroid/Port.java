package swu.cs499.nmapforandroid;

/**
 * Created by swu on 5/8/17.
 */

public class Port {
    int num;
    String type;
    String service;
    //String version;

    public Port(int num, String type, String service) {
        this.num = num;
        this.type = type;
        this.service = service;
    }

    public int getPort() {
        return num;
    }

    public String getType() {
        return type;
    }

    public String getService() {
        return service;
    }

    /*
    public String getVersion() {
        return version;
    }
    */
}
