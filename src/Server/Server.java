package Server;

import Agent.AgentImpl;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;

public class Server extends Thread {

    private final Socket agentSocket;
    private String lockID;

    private Hashtable<String, Object> serverServices = new Hashtable<>();

    private static volatile boolean isRunning = true;

    public Server(Socket s, boolean isTarget) {
        this.agentSocket = s;

        if (isTarget) {
            serverServices.put("imageProcessing", new Service("imageRecognition"));
        }
    }
    
    public static void stopServer(String host, int port) {
        isRunning = false;

        // Socket pour débloquer le serveur d'origine
        try (Socket ignored = new Socket(host, port)) {
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void launchAgent(AgentImpl agent) {
        Object lock = new Object();
        lockID = agent.getClass().getName() + "_lock";
        serverServices.put(lockID, lock);

        agent.setServerServices(serverServices);

        synchronized(lock) {
            new Thread(() -> {
                try {
                    agent.main();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            try {
                lock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void run() {
        try {
            InputStream agentIS = agentSocket.getInputStream();
            DataInputStream dataIS = new DataInputStream(agentIS);

            int jarLength = dataIS.readInt();
            byte[] jar = dataIS.readNBytes(jarLength);

            int objectLength = dataIS.readInt();
            byte[] objectBytes = dataIS.readNBytes(objectLength);

            AgentClassLoader classLoader = new AgentClassLoader(jar, jarLength);

            try (ObjectInputStream objectIS = new AgentObjectInputStream(
                new ByteArrayInputStream(objectBytes), classLoader)) {
                AgentImpl agent = (AgentImpl) objectIS.readObject();
                agent.setOwnCode(jar);
                launchAgent(agent);
            }

            serverServices.remove(lockID);

            agentIS.close();
            dataIS.close();
            agentSocket.close();

        } catch (Exception e) {
            if (isRunning) throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws IOException {

        if (args.length < 3) {
             System.err.println("Usage: [-o|-t] ipAddress portNumber");
        }

        boolean isOrigin = args[0].contains("-o");
        String host = args[1];
        int port = Integer.parseInt(args[2]);

        try (ServerSocket ss = new ServerSocket(port)) {

            if (isOrigin) {
                Class<?> agentClass = Class.forName("Agent.Agent");
                AgentImpl agent = (AgentImpl) agentClass
                        .getConstructor(String.class, int.class)
                        .newInstance(host, port);

                new Server(null, false).launchAgent(agent);
            }

            while(isRunning) {
                Socket s = ss.accept();

                // Si l'agent a envoyé le signal d'arrêt, nous devons ignorer la connexion fantôme
                if (!isRunning) {
                    s.close();
                    break;
                }
                
                new Server(s, true).start();
            }
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
