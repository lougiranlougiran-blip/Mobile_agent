package Agent;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import Server.Node;

public class AgentImpl implements IAgent {

    // -------------- init ------------------
    private Node origin;
    private String name;

    // ------------- socket -----------------
    private Socket clientSocket;

    // ------------- streams (TO NOT SERIALIZED) ----------------
    private transient OutputStream socketOS;
    private transient ObjectOutputStream objectOS;
    private transient DataOutputStream dataOS;

    // -------------- moves ------------------

    private Node previous;
    private Node next;
    private int index;

    // -------------- services ---------------

    private Hashtable<String, Object> serverServices;

    // private HashMap<String, Integer> nodes = new HashMap<>();
    private final List<Node> nodes = Arrays.asList(
            new Node("localhost", 2001),
            new Node("localhost", 2002),
            new Node("localhost", 2003)
    );

    private final List<String> classList = Arrays.asList(
            "Agent/Service",
            "Agent/AgentImpl",
            "Agent/IAgent",
            "Agent/MoveException",
            "Agent/Agent.Jarfactory",
            "Server/Node"
    );

    public void init(String name, Node origin) {
        this.name = name;
        this.origin = origin;
        index = 0;
        next = nodes.get(index);
        previous = null;
    }

    public void startConnection(String ip, int port) {
        try {
            clientSocket = new Socket(ip, port);
            socketOS= clientSocket.getOutputStream();
            objectOS = new ObjectOutputStream(socketOS);
            dataOS = new DataOutputStream(socketOS);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessage() throws IOException {
        JarFactory jw = new JarFactory(classList);

        byte[] code = jw.createJar();

        dataOS.writeInt(code.length);    // On envoie la longueur du code
        socketOS.write(code);            // Ecritude du code
        socketOS.flush();                // Envoi immediat

        objectOS.writeObject(this);
        objectOS.flush();
    }

    public void stopConnection() throws IOException {
        dataOS.close();
        objectOS.close();
        socketOS.close();
        clientSocket.close();
    }

    public void goNext() {
        previous = next;
        next = nodes.get(index++);
    }

    public void goBack() {
        previous = next;
        next = null;
        index = 0;
    }

    public boolean canMove() {
        return index < nodes.size();
    }

    public Node getTarget() {
        return next;
    }

    public String getName() {
        return name;
    }

	public void setNameServer(Hashtable<String,Object> ns) {
        this.serverServices = ns;
    }

	public Hashtable<String,Object> getNameServer() {
        return serverServices;
    }

	public void move() throws IOException {
        goNext();
        startConnection(next.getIP(), next.getPort());
        sendMessage();
        stopConnection();
    }

	public void back() throws IOException {
        goBack();
        startConnection(origin.getIP(), origin.getPort());
        sendMessage();
        stopConnection();
    }

	public void main() throws IOException {
        // @TODO
        // Code executer par l'agent
        // À remplacer par une application (reseau de neurone, ...)
        // En utilisant le nameServer
        System.out.println("Hello");
    }
}
