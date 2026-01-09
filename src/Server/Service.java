package Server;

import Loader.MNISTLoader;
import java.io.IOException;

public class Service {
    /* Implémente un service proposé par un serveur.
     * Ici un réseau de neurones pour la reconnaissance d'images MNIST.
     */

    private String name;
    private String path = "src/resources/MNIST/";

    public Service(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /* Fonction qui récupère un batch de 'count' données à partir de l'index 'start' dans le dataset MNIST */
    public double[][] getBatchData(int start, int count) {
        try {
            System.out.println("Service loading data from " + start + " to " + (start + count));
            return MNISTLoader.getTestBatchData(path, start, count);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /* Fonction qui récupère les labels associés à un batch de 'count' données à partir de l'index 'start' dans le dataset MNIST */
    public double[] getBatchLabels(int start, int count) {
        try {
            return MNISTLoader.getTestBatchLabels(path, start, count);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
