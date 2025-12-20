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
            new Node("192.168.0.43", 2001),
            new Node("192.168.0.43", 2002),
            new Node("192.168.0.28", 2003),
            new Node("192.168.0.28", 2004)
        );

        origin = new Node(host, port);

        init(nodes, origin, name);

        this.net = NeuralNetwork.LoadFromFile("src/resources/model.txt");
    }

    @Override
    public void process() {
        Service s = (Service) serverServices.get("imageProcessing");
        System.out.println("Processing with: " + s.getName());
        

        double[][] inputData = s.getInputData();
        double[] inputLabels = s.getInputLabels();
        double[] output = net.PredictAllClasses(inputData);


        for (int i = 0; i < output.length; i++) {
            if (output[i] != inputLabels[i]) {
                MNISTLoader.DisplayImage(inputData[i]);
                System.out.println("\u001B[31m" + "Prediction incorrect: " + output[i] + ". Attendue: " + inputLabels[i] + "\u001B[0m");
                System.out.println("-------------------------------------------------------");
            }
        }
        
        predictions.add(net.DisplayTestAccuracy(inputData, inputLabels));

        System.out.print("\n\n");
    }

    @Override
    public void onComeBack() {
        System.out.print("\n\n");
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
