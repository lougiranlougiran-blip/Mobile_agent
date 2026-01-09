package Server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;


public class ServiceMeteoImp extends UnicastRemoteObject implements ServiceMeteo {

    private String name;
    private String path = "src/resources/Meteo/";


    public ServiceMeteoImp(String typeDonne) throws RemoteException {
        this.name = typeDonne;
    }

    public String getName() throws RemoteException {
        return name;
    }

    public double[][] getTemperatureData(int count) throws RemoteException {
        if (name.equals("Temperature")) {
            // renvoyer les n lignes du fichier
            System.out.println("revoie de la " + name);
            return loadCSV(path + "Temperature.csv", count);
        } else {
            System.out.println("Le serveur n'as pas la donnée : " + name);
            return new double [0][0];
        }
    }

    public double[][] getHumiditeData(int count) throws RemoteException {
        if (name.equals("Humidite")) {
            // renvoyer les n lignes du fichier 
            System.out.println("revoie de l'" + name);
            return loadCSV(path + "Humidite.csv", count); 
        } else {
            System.out.println("Le serveur n'as pas la donnée : " + name);
            return new double [0][0];
        }
    }

    public double[][] getPressionData(int count) throws RemoteException {
        if (name.equals("Pression")) {
            // renvoyer les n lignes du fichier 
            System.out.println("revoie de la " + name);
            return loadCSV(path + "Pression.csv", count); 
        } else {
            System.out.println("Le serveur n'as pas la donnée : " + name);
            return new double [0][0];
        }
    }

    private static double[][] loadCSV(String path, int N) {
        List<double[]> rows = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            boolean firstLine = true;
            Integer numberOfLine = 0;

            while ((line = br.readLine()) != null || numberOfLine < N) {
                // Ignorer l'en-tête
                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                String[] parts = line.split(",");
                if (parts.length < 2) continue;

                double value = Double.parseDouble(parts[1].trim());
                rows.add(new double[] { value });
                numberOfLine ++;
            }

        } catch (IOException | NumberFormatException e) {
            System.err.println("Erreur chargement CSV : " + path);
            e.printStackTrace();
        }

        return rows.toArray(new double[0][]);

    }
}
