package Client;

import Server.ServiceMeteo;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;



// client qui utilise le service météo
public class ClientTemp {
    
    public static void main (String args[]) throws NotBoundException, MalformedURLException, RemoteException {
    
        Double Temperature = 0.;
        Double Pression = 0.; 
        Double Humidite = 0.;


        if (args.length < 3) {
            System.err.println("Usage : java ClientTemp <portTemperature> <ipTemperature> (<portPression> <ipPression> <portHumidite> <ipHumidite>) [size]");
            System.exit(1);
        }

        for (int index = 0; index < args.length - 1; index = index + 2) {
            // on va sur la machine suivante, on recupère le stub rmi
            ServiceMeteo s = (ServiceMeteo) Naming.lookup("//" + args[index + 1] + ":" + args[index] + "/ServiceMeteoImp");
            int count = Integer.parseInt(args[args.length - 1]);

            // en fonction de sur quel serveur on est, on ne recupère pas les mêmes données : 3 serveurs, 1 pour chaque donnée
            switch (index) {
                case 0 :
                    double[][] tmp = s.getTemperatureData(count);
                    Temperature = average(tmp);
                    break;
                case 2 :
                    double[][] pres = s.getPressionData(count);
                    Pression = average(pres);
                    break;
                case 4 :
                    double[][] hum = s.getHumiditeData(count);
                    Humidite = average(hum);
                    break;
                default:
                    throw new AssertionError();
            }
        }

        


        /** fin de la recuperation des données 
         * on ecrit les données dans le terminal
        */
        System.out.println("Temperature : " + String.valueOf(Temperature));
        System.out.println("Humidite : " + String.valueOf(Humidite));
        System.out.println("Pression : " + String.valueOf(Pression));
        System.out.print("\n\n");


    }

    /** fonction privé permettant de calculer la moyenne des données renvoyé par le serveur */
    private static double average(double[][] data) {
        // si la data est vide, on evite les erreurs 
        if (data == null || data.length == 0) {
            return 0.0;
        }
        // calcule de la moyenne
        double sum = 0.0;
        for (double[] row : data) {
            sum += row[0];
        }
        return sum / data.length;
    }
}