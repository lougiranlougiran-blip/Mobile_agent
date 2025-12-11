package Server;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends Thread {

    Socket agentSocket;

    // @TODO Creer le nameServer

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
            ObjectInputStream objectIS = new ObjectInputStream(agentIS);

            // @TODO

            // Recception de la taille du code

            // Reception du code

            // Creation d'un ClassLoader

            // On donne le code au ClassLoader pour traitement

            // Reception des donnees (etat de l'agent)

            // Deserialiser les donnees avec le ClassLoader

            // ...

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
