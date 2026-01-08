package Server;

import Loader.MNISTLoader;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ServiceImp extends UnicastRemoteObject implements Service {

    private String name;
    private String path = "src/resources/MNIST/";

    public ServiceImp(String name) throws RemoteException {
        this.name = name;
    }

    public String getName() throws RemoteException {
        return name;
    }

    public double[][] getBatchData(int start, int count) throws RemoteException {
        try {
            System.out.println("Service loading data from " + start + " to " + (start + count));
            return MNISTLoader.getTestBatchData(path, start, count);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public double[] getBatchLabels(int start, int count) throws RemoteException {
        try {
            return MNISTLoader.getTestBatchLabels(path, start, count);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
