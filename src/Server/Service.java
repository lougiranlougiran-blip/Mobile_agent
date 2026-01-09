package Server;

import Loader.MNISTLoader;
import java.io.IOException;

public class Service {

    private String name;
    private String path = "src/resources/MNIST/";

    public Service(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public double[][] getBatchData(int start, int count) {
        try {
            System.out.println("Service loading data from " + start + " to " + (start + count));
            return MNISTLoader.getTestBatchData(path, start, count);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public double[] getBatchLabels(int start, int count) {
        try {
            return MNISTLoader.getTestBatchLabels(path, start, count);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
