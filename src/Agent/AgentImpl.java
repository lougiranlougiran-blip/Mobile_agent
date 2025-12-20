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

public class AgentImpl implements IAgent {

    // -------------- init ------------------
    private Agent service;

    // ------------- socket -----------------
    private Socket clientSocket;

    // ------------- streams (TO NOT SERIALIZED) ----------------
    private OutputStream socketOS;
    private ByteArrayOutputStream objectBAOS;
    private ObjectOutputStream objectOS;
    private DataOutputStream dataOS;

    // -------------- services ---------------

    protected Hashtable<String, Object> serverServices;

    private final List<String> classList = Arrays.asList(
            "Agent.Service",
            "Agent.AgentImpl",
            "Agent.IAgent",
            "Agent.Jarfactory",
            "Server.Node"
    );

    public void init(Agent service) {
        this.service = service;
    }

    public void startConnection(String ip, int port) {
        try {
            clientSocket = new Socket(ip, port);

            socketOS= clientSocket.getOutputStream();
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
        byte[] codeBytes = JarFactory.createJar(classList);
        byte[] objectBytes = serializeObject(service);

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

	public void setServerServices(Hashtable<String,Object> ns) {
        this.serverServices = ns;
    }

	public Hashtable<String,Object> getServerServices() {
        return serverServices;
    }

	public void move() throws IOException {
        service.goNext();
        startConnection(service.nextGetIP(), service.nextGetPort());
        sendMessage();
        stopConnection();
    }

	public void back() throws IOException {
        service.goBack();
        startConnection(service.getOrigin().getIP(), service.getOrigin().getPort());
        sendMessage();
        stopConnection();
    }

	public void main() throws IOException {}
}
