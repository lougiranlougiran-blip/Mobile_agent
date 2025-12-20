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

    public void sendMessage() throws IOException {
        List<String> classList = Arrays.asList(
            "Agent.AgentImpl",
            "Agent.IAgent",
            "Agent.JarFactory",
            "Server.Service",
            "Server.Node",
            this.getClass().getName()
        );

        byte[] codeBytes = JarFactory.createJar(classList);
        byte[] objectBytes = serializeObject(this);

        dataOS.writeInt(codeBytes.length);
        dataOS.write(codeBytes);
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
	public void move() throws IOException {
        this.goNext();
        startConnection(this.getTargetHost(), this.getTargetPort());
        sendMessage();
        stopConnection();
    }

    @Override
	public void back() throws IOException {
        this.goBack();
        startConnection(this.getOrigin().getHost(), this.getOrigin().getPort());
        sendMessage();
        stopConnection();
    }

    public Node getOrigin() {
        return origin;
    }

    public String getTargetHost() {
        return next.getHost();
    }

    public int getTargetPort() {
        return next.getPort();
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

    public Node getPrevious() {
        return previous;
    }

    @Override
    public void main() throws IOException {

        if (getTarget() == null) {
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
            System.out.println("Agent " + name + "has successfully started.");
            System.out.println("Agent moving to: " + getTarget());
            move();
        } else if (canMove()) {
            System.out.println("Agent coming from: " + getPrevious());
            process();
            System.out.println("Agent moving to: " + getTarget());
            move();
        } else {
            System.out.println("Agent coming from: " + getPrevious());
            process();
            System.out.println("Agent moving to: " + getOrigin());
            back();
        }
    }

    public abstract void process();
    public abstract void onComeBack();
}
