package Agent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import Model.NeuralNetwork;
import Loader.MNISTLoader;

import Server.Node;
import Server.Service;

public class Agent extends AgentImpl {

    private NeuralNetwork net;
    private List<Double> predictions = new ArrayList<>();

    private String name = "Agent";
    private Node origin;

    public Agent(String host, int port) {
        super();

        List<Node> nodes = Arrays.asList(
            new Node("127.0.0.1", 2001),
            new Node("127.0.0.1", 2002),
            new Node("127.0.0.1", 2003),
            new Node("127.0.0.1", 2004)
        );

        origin = new Node(host, port);

        init(nodes, origin, name);

        this.net = NeuralNetwork.LoadFromFile("src/resources/model.txt");
    }

    @Override
    public void process() {
        Service s = (Service) serverServices.get("imageProcessing");
        System.out.println("Using service: " + s.getName());

        int totalDatasetSize = 10_000;
        int partitionSize = totalDatasetSize / nodes.size();
        int currentNodeIndex = (index - 1 + nodes.size()) % nodes.size();
        int start = currentNodeIndex * partitionSize;

        if (currentNodeIndex == nodes.size() - 1) {
            partitionSize = totalDatasetSize - start; 
        }

        double[][] inputData = s.getBatchData(start, partitionSize);
        double[] inputLabels = s.getBatchLabels(start, partitionSize);
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
    }

    @Override
    public void onComeBack() {
        System.out.print("\n");
        double finalAccuracy = 0.0;

        for (double accuracy : predictions) {
            finalAccuracy += accuracy;
        }

        System.out.println("Accuracy (final result) : " + (finalAccuracy / predictions.size()));
    }

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
