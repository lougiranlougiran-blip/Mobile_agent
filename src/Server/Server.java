package Server;

import Agent.IAgent;
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
    /* Représente un serveur capable de recevoir et exécuter des agents mobiles. 
     * Nous considérons que chaque machine peut jouer le rôle d'agent ou de serveur.
     * Selon les paramètres passés au lancement, le serveur peut donc initialiser un agent ou un service.
     * Nous avons choisi cette architecture en suivant les conseils de Camélia Slimani. Cela permet entre autre
     * de simplifier le déploiement des agents mobiles, les tests et les démonstrations. Cependant, si nous
     * voulons gérer proprement la fermeture du serveur, nous devons implémenter des mécanismes plus complexes (voir ci-dessous).
     */

    private final Socket agentSocket; // Socket pour communiquer avec l'agent
    private String lockID;            // ID du lock pour la synchronisation avec l'agent
    private static String meteoType; // type de meteo a laquel on a acces

    private Hashtable<String, Object> serverServices = new Hashtable<>();  // Services proposés par le serveur

    private static volatile boolean isRunning = true;  // Indique si le serveur est en cours d'exécution

    public Server(Socket s, boolean isTarget) {
        this.agentSocket = s;

        // Si le serveur n'est pas l'origine, il propose des services
        if (isTarget) {
            serverServices.put("imageProcessing", new Service("imageRecognition"));
            serverServices.put("meteo", new ServiceMeteo(meteoType));
        }
    }
    
    /* Fonction pour arrêter le serveur proprement (optionnelle).
     * Elle permet concrètement de terminer le programme sur le serveur d'origine d'où part l'agent, lorsque
     * le résultat est disponible.
     */
    public static void stopServer(String host, int port) {
        // On dit au serveur qu'il doit s'arrêter la prochaine fois qu'il acceptera une connexion
        isRunning = false;

        /* La technique est de créer une connexion supplémentaire "fantôme" qui va débloquer le serveur
        toujours en attente de connexions. Le serveur va consulter son état, et terminer l'attente. */
        try (Socket _ = new Socket(host, port)) {
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /* Fonction pour lancer un agent sur le serveur */
    public void launchAgent(IAgent agent) {
        Object lock = new Object();                      // Création d'un verrou pour la synchronisation
        lockID = agent.getClass().getName() + "_lock";   // On donne une ID unique au verrou
        serverServices.put(lockID, lock);                // On met à disposition le verrou via les services

        agent.setServerServices(serverServices);         // On donne les services à l'agent

        synchronized(lock) {                             
            new Thread(() -> {                           // Lancement de l'agent dans un thread séparé
                try {
                    agent.main();                        // Exécution de la méthode principale de l'agent
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

    /* Fonction exécutée lors du démarrage du thread */
    @Override
    public void run() {
        try {
            InputStream agentIS = agentSocket.getInputStream();                   // On récupère le flux du socket
            DataInputStream dataIS = new DataInputStream(agentIS);                // Flux pour lire les tailles des données

            int jarLength = dataIS.readInt();                                     // Taille du JAR
            byte[] jar = dataIS.readNBytes(jarLength);                            // Contenu du JAR

            int objectLength = dataIS.readInt();                                  // Taille de l'agent
            byte[] objectBytes = dataIS.readNBytes(objectLength);                 // Contenu de l'agent

            AgentClassLoader classLoader = new AgentClassLoader(jar, jarLength);  // Création du ClassLoader pour l'agent

            try (ObjectInputStream objectIS = new AgentObjectInputStream(         // Lecture de l'objet avec le ClassLoader personnalisé
                new ByteArrayInputStream(objectBytes), classLoader)) {
                IAgent agent = (IAgent) objectIS.readObject();
                agent.setOwnCode(jar);                                            // On sauvegarde le code dans l'agent
                launchAgent(agent);                                               // On démarre l'agent
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
             System.err.println("Usage: [-o|-t] ipAddress portNumber <type de meteo : t p h, defaut temperature> <type agent NN Meteo, defaut NN>");
        }

        // On vérifie si le serveur est l'origine ou une cible
        boolean isOrigin = args[0].contains("-o");
        // Récupération de l'adresse et du port
        String host = args[1];
        int port = Integer.parseInt(args[2]);

        // initialisation du serveur meteo, acces a 1 seul donnee
        switch (args[3]) {
          case "t" :
              meteoType = "Temperature";
              break;
          case "h" :
              meteoType = "Humidite";
              break;
          case "p" :
              meteoType = "Pression";
              break;
          default:
              meteoType = "Temperature";
        }

        /* On écoute les connexions entrantes. Pour un noeud quelconque, cela permet d'attendre l'agent.
         * Pour le serveur d'origine, cela permet de récupérer le résultat, c'est un serveur du point de vue
         * de n'importe quel autre serveur. 
         * Si le serveur est l'origine, on créé l'agent dynamiquement. En réalité, c'est une classe
         * Agent.Agent, mais pour que les autres serveurs trouvent la classe, il faut le charger en tant qu'interface.
         */
        try (ServerSocket ss = new ServerSocket(port)) {

            if (isOrigin) {
                if (args[4].contains("Meteo")) {
                    Class<?> agentClass = Class.forName("Agent.AgentMeteo");   // Chargement de la classe ayant pour nom le nom passé en paramètres
                    IAgent agent = (IAgent) agentClass
                            .getConstructor(String.class, int.class)           // On récupère le constructeur avec les paramètres
                            .newInstance(host, port);                          // Création d'une instance de l'agent

                    new Server(null, false).launchAgent(agent);    // On lance l'agent immédiatement sur l'origine
                } else { 
                    Class<?> agentClass = Class.forName("Agent.Agent");
                    IAgent agent = (IAgent) agentClass
                            .getConstructor(String.class, int.class)
                            .newInstance(host, port);

                    new Server(null, false).launchAgent(agent);
                }
            }

            // Attente des connexions entrantes
            while(isRunning) {
                // Création d'un socket TCP.
                Socket s = ss.accept();

                /* Si l'agent a envoyé le signal d'arrêt, la connexion précédente est la connexion fantôme
                   On l'ignore et on stop le serveur */
                if (!isRunning) {
                    s.close();
                    break;
                }
                
                // Lancement d'un nouveau thread pour gérer la connexion entrante
                new Server(s, true).start();
            }
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
