package Server;

import java.io.Serializable;

public class Node implements Serializable {

    private final String host;
    private final int port;

    public Node(String host, int p) {
        this.host = host;
        this.port = p;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return host + "  -  " + port;
    }
}
