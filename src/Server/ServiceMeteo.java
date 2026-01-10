package Server;

import java.rmi.Remote;
import java.rmi.RemoteException;


/* 
Interface du service renvoyé au client lors de sa connextion.
*/
public interface ServiceMeteo extends Remote {


    String getName() throws RemoteException;

    double[][] getTemperatureData(int count) throws RemoteException;

    double[][] getHumiditeData(int count) throws RemoteException;

    double[][] getPressionData(int count) throws RemoteException;

}
