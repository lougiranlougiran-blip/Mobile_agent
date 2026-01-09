package Client;

import Server.ServiceMeteo;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;



// client qui utilise un autre service
public class ClientTemp {
    
    public static void main (String args[]) throws NotBoundException, MalformedURLException, RemoteException {
    
        Double Temperature = 0.;
        Double Pression = 0.; 
        Double Humidite = 0.;


        if (args.length < 4) {
            System.err.println("Usage : java ServerRMI <portTemperature> <portHumidite> <portPresssion> <number of data>");
            System.exit(1);
        }

        for (int index = 0; index < 3; index++) {
            ServiceMeteo s = (ServiceMeteo) Naming.lookup("//localhost:" + args[index] + "/ServiceMeteoImp");
            // System.out.println("Using service: " + s.getName());

            int count = Integer.parseInt(args[3]);


            switch (index) {
                case 0 :
                    double[][] tmp = s.getTemperatureData(count);
                    Temperature = average(tmp);
                    break;
                case 1 :
                    double[][] hum = s.getHumiditeData(count);
                    Humidite = average(hum);
                    break;
                case 2 :
                    double[][] pres = s.getPressionData(count);
                    Pression = average(pres);
                    break;
                default:
                    throw new AssertionError();
            }
        }

        // System.out.println("Temperature : " + String.valueOf(Temperature));
        // System.out.println("Humidite : " + String.valueOf(Humidite));
        // System.out.println("Pression : " + String.valueOf(Pression));
        // System.out.print("\n\n");

        


        /** fin de la recuperation des données */
        System.out.println("Temperature : " + String.valueOf(Temperature));
        System.out.println("Humidite : " + String.valueOf(Humidite));
        System.out.println("Pression : " + String.valueOf(Pression));
        System.out.print("\n\n");


    }

    private static double average(double[][] data) {
        if (data == null || data.length == 0) {
            return 0.0;
        }

        double sum = 0.0;
        for (double[] row : data) {
            sum += row[0];
        }
        return sum / data.length;
    }
}