package Client;

import Server.Service;
import Server.ServeurRMI;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import Model.NeuralNetwork;
import Loader.MNISTLoader;


public class Client {
    
    private static NeuralNetwork net;
    private static List<Double> predictions = new ArrayList<>();

    public static void main(String args[]) throws NotBoundException, MalformedURLException, RemoteException {
        /*consulte les 3 serveurs suivant dont les ports sont donnée en argument */
        if (args.length < 3) {
            System.err.println("Usage : java ServerRMI <port1> <port2> <port3>");
            System.exit(1);
        }

        net = NeuralNetwork.LoadFromFile("src/resources/model.txt");
        // Integer port1 = Integer.parseInt(args[0]);
        // Integer port2 = Integer.parseInt(args[1]);
        // Integer port3 = Integer.parseInt(args[2]);

        // Service service1 = (Service) Naming.lookup("//localhost:" + args[0] + "/ServiceImp");
        // Service service2 = (Service) Naming.lookup("//localhost:" + args[1] + "/ServiceImp");
        // Service service3 = (Service) Naming.lookup("//localhost:" + args[2] + "/ServiceImp");

        /** a faire sur chaque serveur */
        for (int index = 0; index < 3; index++) {
            Service s = (Service) Naming.lookup("//localhost:" + args[index] + "/ServiceImp");;
            // System.out.println("Using service: " + s.getName());

            int totalDatasetSize = 10_000;
            int partitionSize = totalDatasetSize / 3;
            int currentNodeIndex = index;
            int start = currentNodeIndex * partitionSize;

            if (currentNodeIndex == 3 - 1) {
                partitionSize = totalDatasetSize - start; 
            }
            System.out.println("debug");
            System.out.println(start);
            System.out.println(partitionSize);
            System.out.println(args[0]);
            // double[][] inputData = s.getBatchData(start, partitionSize);
            
            try {
                double[][] inputData = s.getBatchData(start, partitionSize);
                System.out.println("Données reçues : " + inputData.length + " lignes");
                double[] inputLabels = s.getBatchLabels(start, partitionSize);
                System.out.println("Données reçues : " + inputLabels.length + " lignes");

                double[] output = net.PredictAllClasses(inputData);
          



            for (int i = 0; i < output.length; i++) {
                if (output[i] != inputLabels[i]) {
                    MNISTLoader.DisplayImage(inputData[i]);
                    System.out.println("\u001B[31m" 
                        + "Prediction incorrect: " + output[i] + ". Attendue: " + inputLabels[i] + "\u001B[0m"
                    );
                    System.out.println("-------------------------------------------------------");
                }
            }
            
            predictions.add(net.DisplayTestAccuracy(inputData, inputLabels));

            System.out.print("\n\n");

            } catch (RemoteException e) {
                System.err.println("Erreur lors de l'appel RMI : " + e.getMessage());
                e.printStackTrace();
            }
        }


        /** fin de la recuperation des données */

        System.out.print("\n");
        double finalAccuracy = 0.0;

        for (double accuracy : predictions) {
            finalAccuracy += accuracy;
        }

        System.out.println("Accuracy (final result) : " + (finalAccuracy / predictions.size()));
    }
} 