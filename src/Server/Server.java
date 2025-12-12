package Server;

import Agent.AgentImpl;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;

public class Server extends Thread {

    private final Socket agentSocket;

    // TODO
    private Hashtable<String, Object> serverServices;

    public Server(Socket s) {
        this.agentSocket = s;
    }

    public static void main(String[] args) throws IOException {

        if (args.length != 2) {
             System.err.println("Usage: " + args[0] + " " + "portNumber");
        }

        int port = Integer.parseInt(args[1]);

        try (ServerSocket ss = new ServerSocket(port)) {

            while(true) {
                Socket s = ss.accept();
                new Server(s).start();
            }
        }
    }

    public void run() {
        try {
            InputStream agentIS = agentSocket.getInputStream();
            DataInputStream dataIS = new DataInputStream(agentIS);

            int length = dataIS.readInt();
            byte[] jar = agentIS.readNBytes(length);

            AgentClassLoader classLoader = new AgentClassLoader(jar, length);

            try (ObjectInputStream objectIS = new AgentObjectInputStream(agentIS, classLoader)) {
                AgentImpl agent = (AgentImpl) objectIS.readObject();

                agent.setNameServer(serverServices);
                agent.main();
            }

            agentIS.close();
            dataIS.close();
            agentSocket.close();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
