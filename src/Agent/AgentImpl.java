package Agent;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import Server.Node;

public class AgentImpl implements IAgent {

    private Node origin;

    private String name;

    private Socket clientSocket;

    public AgentImpl()  {
 
    }

    public void init(String name, Node origin) {
        this.name = name;
        this.origin = origin;
    }

    public void startConnection(String ip, int port) {
        try {
            clientSocket = new Socket(ip, port);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }

    public void sendMessage(String message) {

    }

    public void stopConnection() {

    }

	public void setNameServer(Hashtable<String,Object> ns) {

    }

	public Hashtable<String,Object> getNameServer() {

    }

	public void move(Node target) throws MoveException {

    }

	public void back() throws MoveException {

    }

	public void main() throws MoveException {

    }
}