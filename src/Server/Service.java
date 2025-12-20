package Server;

import java.io.IOException;

import Loader.MNISTLoader;

public class Service {

    private String name;
    private double[][] testData;
    private double[] testLabels;
    private String path = "src/resources/MNIST/";

    public Service(String name) {
        this.name = name;
        init();
    }

    public void init() {
        try {
            testData = MNISTLoader.getTestData(path);
            testLabels = MNISTLoader.getTestLabels(path);
            System.out.println("testData: length=" + testData.length + " features=" + testData[0].length);
            System.out.println("testLabels: length=" + testLabels.length);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getName() {
        return name;
    }

    public double[][] getInputData() {
        return testData;
    }

    public double[] getInputLabels() {
        return testLabels;
    }
}