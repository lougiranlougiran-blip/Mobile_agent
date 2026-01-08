package Server;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

/**
 * Class for the RMI server that starts the registry and create the pads
 *
 * @author you
 */
public final class ServeurRMI {

  public static String serverURI = "//localhost:";

  public static void main(String args[]) {
    try {
        /* recuperation de l'argument : port sur la machine*/

        if (args.length < 1) {
            System.err.println("Usage : java ServerRMI <port>");
            System.exit(1);
        }
        String port = args[0];
        serverURI = serverURI + port;

        /* Launching the naming service – rmiregistry – within the JVM */
        LocateRegistry.createRegistry(Integer.parseInt(port));

        // Création du service
        Service service = new ServiceImp(port);

        // Publication du service
        Naming.rebind(serverURI + "/ServiceImp", service);

        System.out.println("Serveur RMI prêt");

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
