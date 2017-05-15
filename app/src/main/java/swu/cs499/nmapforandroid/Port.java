package swu.cs499.nmapforandroid;

/**
 * Created by swu on 5/8/17.
 */

public class Port {
    Host h;
    String port;
    String protocol;
    String service;
    String version;

    public Port(Host h, String port, String protocol, String service, String version) {
        this.port = port;
        this.protocol = protocol;
        this.service = service;
        this.version = version;
    }

    public String getPort() {
        return port;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getService() {
        return service;
    }

    public String getVersion() {
        return version;
    }
}
