package Agent;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import Server.Node;
import Server.Server;

public abstract class AgentImpl implements IAgent {

    private transient Socket clientSocket;

    private transient OutputStream socketOS;
    private transient ByteArrayOutputStream objectBAOS;
    private transient ObjectOutputStream objectOS;
    private transient DataOutputStream dataOS;

    protected byte[] ownCode;
    protected boolean start = true;
    protected String name;
    protected Node origin;
    protected Node previous;
    protected Node next;
    protected int index;

    protected transient Hashtable<String, Object> serverServices;

    protected List<Node> nodes;

    @Override
    public void init(List<Node> nodes, Node origin, String name) {
        this.nodes = nodes;
        this.origin = origin;
        this.name = name;
        index = 0;
        next = nodes.get(index);
        previous = null;
    }

    public void startConnection(String host, int port) {
        try {
            clientSocket = new Socket(host, port);

            socketOS = clientSocket.getOutputStream();
            objectBAOS = new ByteArrayOutputStream();
            objectOS = new ObjectOutputStream(objectBAOS);
            dataOS = new DataOutputStream(socketOS);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] serializeObject(Object obj) {
        try {
        objectOS.writeObject(obj);
        objectOS.flush();
        return objectBAOS.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> getRequiredClasses() {
        return Arrays.asList(
            "Agent.AgentImpl",
            "Agent.IAgent",
            "Agent.JarFactory",
            "Server.Service",
            "Server.Node",
            this.getClass().getName()
        );
    }

    public void sendMessage() throws IOException {
        byte[] codeToSend;

        if (ownCode != null) {
            codeToSend = ownCode;
        } else {
            List<String> classesList = getRequiredClasses();
            codeToSend = JarFactory.createJar(classesList);
        }

        byte[] objectBytes = serializeObject(this);

        dataOS.writeInt(codeToSend.length);
        dataOS.write(codeToSend);
        dataOS.writeInt(objectBytes.length);
        dataOS.write(objectBytes);
        objectOS.flush();
    }

    public void stopConnection() throws IOException {
        dataOS.close();
        objectBAOS.close();
        objectOS.close();
        socketOS.close();
        clientSocket.close();
    }

    @Override
	public void setServerServices(Hashtable<String,Object> ns) {
        this.serverServices = ns;
    }

    @Override
	public Hashtable<String,Object> getServerServices() {
        return serverServices;
    }

    @Override
    public void setOwnCode(byte[] code) {
        this.ownCode = code;
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

    @Override
	public void move() throws IOException {
        this.goNext();
        startConnection(next.getHost(), next.getPort());
        sendMessage();
        stopConnection();
    }

    @Override
	public void back() throws IOException {
        this.goBack();
        startConnection(origin.getHost(), origin.getPort());
        sendMessage();
        stopConnection();
    }

    @Override
    public void main() throws IOException {

        if (next == null) {
            onComeBack();
            Server.stopServer(origin.getHost(), origin.getPort());
        } else {
            execute();
        }

        Object o = getServerServices().get(this.getClass().getName()+"_lock");
		synchronized(o) {o.notify();}
    }

    public void execute() throws IOException {
        if (start) {
            start = false;
            System.out.println("Agent " + name + " has successfully started.");
            System.out.println("Agent moving to: " + next);
            move();
        } else if (index < nodes.size()) {
            System.out.println("Agent coming from: " + previous);
            process();
            System.out.println("Agent moving to: " + next);
            move();
        } else {
            System.out.println("Agent coming from: " + previous);
            process();
            System.out.println("Agent moving to: " + origin);
            back();
        }
    }

    public abstract void process();
    public abstract void onComeBack();
}
