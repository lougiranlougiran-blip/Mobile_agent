package Model;

import Losses.*;

import java.io.*;
import java.util.Arrays;


public class NeuralNetwork implements Serializable {

    private final Layer[] layers;
    private static ILoss lossFunction;
    private final int[] layerSizes;
    private final String loss;
    private final String hiddenActivation;
    private final String outputActivation;

    public NeuralNetwork(int[] layerSizes, String loss, String hiddenActivation, String outputActivation) {
        this.layerSizes = layerSizes;
        this.loss = loss;
        this.hiddenActivation = hiddenActivation;
        this.outputActivation = outputActivation;

        layers = new Layer[layerSizes.length - 1];
        ScanLossFunction(loss);
        InitLayers(layerSizes, hiddenActivation, outputActivation);
    }

    public NeuralNetwork(double[][][] initialWeights, double[][] initialBiases, int[] layerSizes,
                         String loss, String hiddenActivation, String outputActivation) {
        this.layerSizes = layerSizes;
        this.loss = loss;
        this.hiddenActivation = hiddenActivation;
        this.outputActivation = outputActivation;

        layers = new Layer[layerSizes.length - 1];
        ScanLossFunction(loss);
        InitLayers(initialWeights, initialBiases, layerSizes, hiddenActivation, outputActivation);
    }

    public void ScanLossFunction(String loss) {
        switch(loss) {
            case "mean_squared_error":
                lossFunction = new MeanSquaredError();
                break;
            case "cross_entropy":
                lossFunction = new CrossEntropy();
                break;
            default:
                System.err.println("Unknown loss function.");
                System.exit(-1);
        }
    }

    public int[] getLayerSizes() {
        return layerSizes;
    }

    public String getLoss() {
        return loss;
    }

    public String getHiddenActivation() {
        return hiddenActivation;
    }

    public String getOutputActivation() {
        return outputActivation;
    }

    public static ILoss getLossFunction() {
        return lossFunction;
    }

    public void InitLayers(double[][][] initialWeights, double[][] initialBiases,
                           int[] layerSizes, String hiddenActivation, String outputActivation) {
        for (int i = 0; i < layers.length; i++) {
            if (i < layers.length - 1) {
                layers[i] = new Layer(initialWeights[i], initialBiases[i],
                        layerSizes[i], layerSizes[i + 1], hiddenActivation);
            } else {
                layers[i] = new Layer(initialWeights[i], initialBiases[i],
                        layerSizes[i], layerSizes[i + 1], outputActivation);
            }
        }
    }

    public void InitLayers(int[] layerSizes, String hiddenActivation, String outputActivation) {
        for (int i = 0; i < layers.length; i++) {
            if (i < layers.length - 1) {
                layers[i] = new Layer(layerSizes[i], layerSizes[i + 1], hiddenActivation);
            } else {
                layers[i] = new Layer(layerSizes[i], layerSizes[i + 1], outputActivation);
            }
        }
    }

    public double[] NNForwardPropagation(double[] input) {
        double[][] activations = {input};

        for (Layer layer: layers) {
            activations = layer.ForwardPropagation(activations);
        }
        return activations[0];
    }

    public double[][] NNForwardPropagationBatch(double[][] inputs) {
        double[][] activations = inputs;

        for (Layer layer: layers) {
            activations = layer.ForwardPropagationBatch(activations);
        }
        return activations;
    }

    public void BackPropagation(double[][] outputs, double[][] expectedOutputs) {
        Layer outputLayer = layers[layers.length - 1];
        double[][] computedOutputGradients = outputLayer.ComputeOutputGradientsBatch(outputs, expectedOutputs);

        for (int layer = layers.length - 2; layer >= 0; layer--) {
            computedOutputGradients = layers[layer].BackPropagationBatch(layers[layer + 1], computedOutputGradients);
        }
    }

    public void UpdateAllWeights(double learningRate, int datasetSize) {
        for (Layer layer : layers) {
            layer.UpdateWeights(learningRate, datasetSize);
        }
    }

    public void BatchGradientDescent(double[][] trainInputs, double[][] expectedOutputs,
                                     double learningRate, int batchSize) {
        int totalSize = trainInputs.length;
        int batchesNumber = (int) Math.ceil((double) totalSize / batchSize);
        double totalLoss = 0;
        int totalCorrect = 0;

        for (int batch = 0; batch < batchesNumber; batch++) {
            int start = batch * batchSize;
            int end = Math.min(start + batchSize, totalSize);

            double[][] batchInputs = Arrays.copyOfRange(trainInputs, start, end);
            double[][] batchOutputs = Arrays.copyOfRange(expectedOutputs, start, end);
            double[][] outputs = NNForwardPropagationBatch(batchInputs);

            BackPropagation(outputs, batchOutputs);

            totalLoss += lossFunction.GlobalLoss(outputs, batchOutputs);
            totalCorrect += GetCorrectPredictions(outputs, batchOutputs);

            UpdateAllWeights(learningRate, batchSize);
        }

        double averageLoss = totalLoss / totalSize;
        double accuracy = (totalCorrect / (double) totalSize) * 100;
        System.out.printf("Loss: %.6f - Accuracy: %.2f%%%n", averageLoss, accuracy);
    }

    public void Train(double[][] trainInputs, double[] expectedOutput, double learningRate,
                      double iterationsNumber, int batchSize, double decay) {
        double[][] expectedOutputs = OneHotEncoder(expectedOutput, trainInputs[0].length);
        double initialLr = learningRate;

        for (int epoch = 0; epoch <= iterationsNumber; epoch++) {
            System.out.print("Epoch " + epoch + " - ");
            BatchGradientDescent(trainInputs, expectedOutputs, learningRate, batchSize);
            learningRate = initialLr / (1 + decay * epoch);
        }
    }

    public double[][] OneHotEncoder(double[] expectedOutput, int numClasses) {
        int inputsNumber = expectedOutput.length;
        double[][] encodedOutputs = new double[inputsNumber][numClasses];

        for (int i = 0; i < inputsNumber; i++) {
            int expectedIndex = (int) expectedOutput[i];
            encodedOutputs[i][expectedIndex] = 1.0;
        }

        return encodedOutputs;
    }

    public int GetCorrectPredictions(double[][] predictions, double[][] expectedOutputs) {
        int correct = 0;
        for (int i = 0; i < predictions.length; i++) {
            int predictedClass = MathsUtilities.IndexMaxOfArray(predictions[i]);
            int expectedClass = MathsUtilities.IndexMaxOfArray(expectedOutputs[i]);

            if (predictedClass == expectedClass) {
                correct++;
            }
        }
        return correct;
    }

    public double[] Predict(double[] testInput) {
        return NNForwardPropagation(testInput);
    }

    public double[][] PredictAll(double[][] testInputs) {
        double[][] predictions = new double[testInputs.length][];

        for (int i = 0; i < predictions.length; i++) {
            predictions[i] = NNForwardPropagation(testInputs[i]);
        }

        return predictions;
    }

    public double PredictClass(double[] testInput) {
        double[] outputs = NNForwardPropagation(testInput);
        if (outputs.length == 1) {
            return outputs[0] >= 0.5 ? 0.0 : 1.0;
        } else {
            return MathsUtilities.IndexMaxOfArray(outputs);
        }
    }

    public double[] PredictAllClasses(double[][] testInputs) {
        double[] predictions = new double[testInputs.length];

        for (int i = 0; i < predictions.length; i++) {
            double[] outputs = NNForwardPropagation(testInputs[i]);

            if (outputs.length == 1) {
                predictions[i] = outputs[0] >= 0.5 ? 0.0 : 1.0;
            } else {
                predictions[i] = MathsUtilities.IndexMaxOfArray(outputs);
            }
        }

        return predictions;
    }

    public void DisplayPredictions(double[][] predictions) {
        for (int i = 0; i < predictions.length; i++) {
            System.out.println(i + "    " + Arrays.toString(predictions[i]));
        }
    }

    public double DisplayTestAccuracy(double[][] inputs, double[] expectedOutput) {
        double[][] expectedOutputs = OneHotEncoder(expectedOutput, inputs[0].length);
        double[][] predictions = OneHotEncoder(PredictAllClasses(inputs), inputs[0].length);
        int correct = GetCorrectPredictions(predictions, expectedOutputs);
        double accuracy = correct / (double) expectedOutput.length;
        System.out.println("\u001B[32m" + "Accuracy: " + accuracy
                + " (" + correct + "/" + expectedOutput.length + ")" + "\u001B[0m");
        return accuracy;
    }

    public int getWeightsNumber() {
        int weightsNumber = 0;

        for (Layer layer: layers) {
            int nbNeurons = layer.getNeuronsNumber();
            weightsNumber += nbNeurons * (1 + layer.getFeaturesNumber());
        }

        return weightsNumber;
    }

    public double[] GetAllWeights() {
        int weightsNumber = getWeightsNumber();
        double[] allWeights = new double[weightsNumber];
        int index = 0;

        for (Layer layer : layers) {
            for (int neuron = 0; neuron < layer.getNeuronsNumber(); neuron++) {
                for (int feature = 0; feature < layer.getFeaturesNumber(); feature++) {
                    allWeights[index++] = layer.getWeights()[neuron][feature];
                }
                allWeights[index++] = layer.getBiases()[neuron];
            }
        }

        return allWeights;
    }

    public void WriteInFile(String filePath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(getLoss());
            writer.newLine();

            writer.write(getHiddenActivation());
            writer.newLine();
            writer.write(getOutputActivation());
            writer.newLine();

            int[] layerSizes = getLayerSizes();
            for (int size : layerSizes) {
                writer.write(size + " ");
            }
            writer.newLine();

            double[] allWeights = GetAllWeights();
            for (double weight : allWeights) {
                writer.write(weight + " ");
            }
            writer.newLine();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static NeuralNetwork LoadFromFile(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String loss = reader.readLine();
            String hiddenActivation = reader.readLine();
            String outputActivation = reader.readLine();

            String[] layerSizesStr = reader.readLine().split(" ");
            int[] layerSizes = Arrays.stream(layerSizesStr).mapToInt(Integer::parseInt).toArray();

            String[] weightsStr = reader.readLine().split(" ");
            double[] allWeights = Arrays.stream(weightsStr).mapToDouble(Double::parseDouble).toArray();

            double[][][] weights = new double[layerSizes.length - 1][][];
            double[][] biases = new double[layerSizes.length - 1][];

            int index = 0;
            for (int l = 0; l < layerSizes.length - 1; l++) {
                int nbNeurons = layerSizes[l + 1];
                int nbFeatures = layerSizes[l];

                weights[l] = new double[nbNeurons][nbFeatures];
                biases[l] = new double[nbNeurons];

                for (int neuron = 0; neuron < nbNeurons; neuron++) {
                    for (int feature = 0; feature < nbFeatures; feature++) {
                        weights[l][neuron][feature] = allWeights[index++];
                    }
                    biases[l][neuron] = allWeights[index++];
                }
            }

            return new NeuralNetwork(weights, biases, layerSizes, loss, hiddenActivation, outputActivation);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
