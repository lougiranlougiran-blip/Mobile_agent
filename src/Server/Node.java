package Server;

import java.io.Serializable;

public class Node implements Serializable {

    private final String ip;
    private final int port;

    public Node(String ip, int p) {
        this.ip = ip;
        this.port = p;
    }

    public String getIP() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return "IP address :  " + ip + "  -  " + "Port number : " + port;
    }
}
