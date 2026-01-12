package Server;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

/**
 * Class for the RMI server that starts the registry and create the pads
 *
 * @author you
 */
public final class ServeurRMI {

  public static String serverURI = "//";

  public static void main(String args[]) {
    try {

        /* recuperation de l'argument : port sur la machine type de donne pour la meteo*/
        if (args.length < 3) {
            System.err.println("Usage : java ServerRMI ipAdress <port> <t Temperature, p pression, h humidite>");
            System.exit(1);
        }
        // initiailsation de l'adresse :
        serverURI = "//" + args[0] + ":" + args[1];
        String port = args[1];
  

        /* Launching the naming service – rmiregistry – within the JVM */
        LocateRegistry.createRegistry(Integer.parseInt(port));

        // Création du service pour le reseau de neurones
        Service service = new ServiceImp(port);
        
        // Création du service pour la Meteo
        String typeServiceMeteo = "";
        switch (args[2]) {
          case "t" :
              typeServiceMeteo = "Temperature";
              break;
          case "h" :
              typeServiceMeteo = "Humidite";
              break;
          case "p" :
              typeServiceMeteo = "Pression";
              break;
          default:
              throw new AssertionError();
        }



        ServiceMeteo serviceMeteo = new ServiceMeteoImp(typeServiceMeteo);

        // Publication des services
        Naming.rebind(serverURI + "/ServiceImp", service);
        Naming.rebind(serverURI + "/ServiceMeteoImp", serviceMeteo);

        System.out.println("Serveur RMI prêt");

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
