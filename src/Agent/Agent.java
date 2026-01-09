package Agent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import Model.NeuralNetwork;
import Loader.MNISTLoader;

import Server.Node;
import Server.Service;

public class Agent extends AgentImpl {
    /* Agent contenant uniquement la logique métier (l'application).
     * Dans ce cas, l'agent utilise un réseau de neurones pour faire des prédictions sur des images
     * du dataset MNIST et afficher ses performances en test. On suppose que le modèle est déjà entraîné.
     * Nous utilisons une librairie personnalisée de réseau de neurones. Elle n'est pas parfaitement optimisée
     * mais elle a été terminée pour les besoins du projet, dans un soucie de présenter un exemple intéressant.
     */

    private NeuralNetwork net;                               // Réseau de neurones utilisé par l'agent
    private List<Double> predictions = new ArrayList<>();    // Une prédiction est une accuracy sur un batch d'images (ex: 0.96)

    private String name = "Agent";
    private Node origin;

    public Agent(String host, int port) {
        super();

        // Liste des noeuds (serveurs) avec les adresses IP et les ports
        List<Node> nodes = Arrays.asList(
            new Node("127.0.0.1", 2001),
            new Node("127.0.0.1", 2002),
            new Node("127.0.0.1", 2003),
            new Node("127.0.0.1", 2004)
        );

        // L'origine est le serveur avec l'IP est le Port passés en paramètre à l'exécution
        origin = new Node(host, port);

        // Initialise la logique de migration sur AgentImpl
        init(nodes, origin, name);

        // Charge le modèle à partir d'un fichier
        this.net = NeuralNetwork.LoadFromFile("src/resources/model.txt");
    }

    /* 
     * Traitement effectuée par l'agent, implementation de la méthode abstraite de AgentImpl.
     * Pour cet exemple, chaque serveur possède des images stockées localement (un partition du dataset MNIST).
     * L'agent récupère les images (sur le serveur), prédit la classe de chaque image puis compare ses résultats
     * avec les résultats attendus. Ici, on affiche les images mal classées (optionnel).
     */
    @Override
    public void process() {
        // On récupère le service "imageProcessing" du serveur courant
        Service s = (Service) serverServices.get("imageProcessing");
        System.out.println("Using service: " + s.getName());

        /* 
        * On simplifie la logique en donnant le dataset complet à chaque serveur puis on récupère
        * à chaque fois une partition différente en fonction de l'index du serveur.
        */
        int totalDatasetSize = 10_000;
        int partitionSize = totalDatasetSize / nodes.size();
        int currentNodeIndex = (index - 1 + nodes.size()) % nodes.size();
        int start = currentNodeIndex * partitionSize;

        // Permet de gérer le cas où le dataset n'est pas divisible par le nombre de serveurs
        if (currentNodeIndex == nodes.size() - 1) {
            partitionSize = totalDatasetSize - start; 
        }

        // Récupération des données et des labels
        double[][] inputData = s.getBatchData(start, partitionSize);
        double[] inputLabels = s.getBatchLabels(start, partitionSize);
        // Prédictions
        double[] output = net.PredictAllClasses(inputData);


        // Comparaison et affichage des résultats
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
    }

    /* Lorsque l'agent a terminé et revient à l'origine, il calcule la moyenne des résultats
     * qu'il a obtenu sur les différents serveurs. Après test, on retrouve bien la précision réelle.
     */
    @Override
    public void onComeBack() {
        System.out.print("\n");
        double finalAccuracy = 0.0;

        for (double accuracy : predictions) {
            finalAccuracy += accuracy;
        }

        System.out.println("Accuracy (final result) : " + (finalAccuracy / predictions.size()));
    }

    /* Liste des classes à envoyer. Pour cet exmeple, nous avons inclut 
     * notre librairie personnalisée de réseau de neurones.
     */
    @Override 
    public List<String> getRequiredClasses() {
        return Arrays.asList(
            "Agent.AgentImpl",
            "Agent.IAgent",
            "Agent.JarFactory",
            "Server.Service",
            "Server.Node",
            "Activations.ReLU",
            "Activations.Sigmoid",
            "Activations.SiLU",
            "Activations.SoftMax",
            "Model.IActivation",
            "Model.ILoss",
            "Model.Layer",
            "Model.MathsUtilities",
            "Model.NeuralNetwork",
            "Losses.CrossEntropy",
            "Losses.MeanSquaredError",
            "Loader.MNISTLoader",
            this.getClass().getName()
        );
    }
}
