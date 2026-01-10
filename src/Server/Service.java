package Server;

import java.rmi.Remote;
import java.rmi.RemoteException;

/* 
Interface du service renvoyé au client lors de sa connextion.
*/
public interface Service extends Remote {

    String getName() throws RemoteException;

    double[][] getBatchData(int start, int count) throws RemoteException;

    double[] getBatchLabels(int start, int count) throws RemoteException;
}
