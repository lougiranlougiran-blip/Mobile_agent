package Client;

import Model.NeuralNetwork;
import Server.Service;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;


public class Client {
     /* Dans ce cas, le client utilise un réseau de neurones pour faire des prédictions sur des images
     * du dataset MNIST et afficher ses performances en test. On suppose que le modèle est déjà entraîné.
     * Nous utilisons une librairie personnalisée de réseau de neurones. Elle n'est pas parfaitement optimisée
     * mais elle a été terminée pour les besoins du projet, dans un soucie de présenter un exemple intéressant.
     */
    
    private static NeuralNetwork net;                               // Réseau de neurones utilisé par le client
    private static List<Double> predictions = new ArrayList<>();    // Une prédiction est une accuracy sur un batch d'images (ex: 0.96)

    public static void main(String args[]) throws NotBoundException, MalformedURLException, RemoteException {
        // On attend au moins un port et une taille
        if (args.length < 2) {
            System.err.println("Usage : java ServerRMI <port1> <port2> ... <portn> [size]");
            System.exit(1);
        }

        int totalDatasetSize = Integer.parseInt(args[args.length - 1]);
        int numServers = args.length - 1;

        net = NeuralNetwork.LoadFromFile("src/resources/model.txt"); 


         /* 
        * Traitement effectuée par le client sur chaque serveur
        * Pour cet exemple, chaque serveur possède des images stockées localement (un partition du dataset MNIST).
        * Le client récupère les images (sur le serveur), prédit la classe de chaque image puis compare ses résultats
        * avec les résultats attendus. Ici, on affiche les images mal classées (optionnel).
        */
        for (int index = 0; index < numServers; index++) {
            String port = args[index];

            // on va sur la machine suivante, on recupère le stub rmi
            Service s = (Service) Naming.lookup("//localhost:" + port + "/ServiceImp");;

            /* 
            * On simplifie la logique en donnant le dataset complet à chaque serveur puis on récupère
            * à chaque fois une partition différente en fonction de l'index du serveur.
            */
            int partitionSize = totalDatasetSize / numServers;
            int start = index * partitionSize;

            // Permet de gérer le cas où le dataset n'est pas divisible par le nombre de serveurs
            if (index == numServers - 1) {
                partitionSize = totalDatasetSize - start; 
            }

            
            try {
                // Récupération des données et des labels
                double[][] inputData = s.getBatchData(start, partitionSize);
                System.out.println("Données reçues : " + inputData.length + " lignes");
                double[] inputLabels = s.getBatchLabels(start, partitionSize);
                System.out.println("Données reçues : " + inputLabels.length + " lignes");
                
                predictions.add(net.DisplayTestAccuracy(inputData, inputLabels));

                System.out.print("\n\n");

            } catch (RemoteException e) {
                System.err.println("Erreur lors de l'appel RMI : " + e.getMessage());
                e.printStackTrace();
            }
        }


        /* 
        * Lorsque le client a terminé, il calcule la moyenne des résultats
        * qu'il a obtenu sur les différents serveurs. Après test, on retrouve bien la précision réelle.
        */

        System.out.print("\n");
        double finalAccuracy = 0.0;

        for (double accuracy : predictions) {
            finalAccuracy += accuracy;
        }

        System.out.println("Accuracy (final result) : " + (finalAccuracy / predictions.size()));
    }
} 