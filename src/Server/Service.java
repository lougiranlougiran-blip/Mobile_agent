package Server;

import java.rmi.Remote;
import java.rmi.RemoteException;


public interface Service extends Remote {

    String getName() throws RemoteException;

    double[][] getBatchData(int start, int count) throws RemoteException;

    double[] getBatchLabels(int start, int count) throws RemoteException;
}
