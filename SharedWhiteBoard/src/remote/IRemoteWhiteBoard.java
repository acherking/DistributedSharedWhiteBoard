package remote;
import java.awt.*;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IRemoteWhiteBoard extends Remote {

    public void painting(String name, Shape temperShape, boolean fromCenter) throws RemoteException;

    public void painted(String name, Shape durableShape, boolean fromCenter) throws RemoteException;

    public boolean enterRequest(String applierName, String remoteHostName) throws RemoteException;

    public boolean enterRequest(String applierName, IRemoteWhiteBoard rwb) throws RemoteException;

    public void dropoutRequest(String applierName) throws RemoteException;

    public void exitCurrentWhiteBoard() throws RemoteException;
}
