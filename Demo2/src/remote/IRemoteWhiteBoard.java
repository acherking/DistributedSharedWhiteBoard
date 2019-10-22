package remote;
import javax.print.DocFlavor;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IRemoteWhiteBoard extends Remote {

    public void painting(String name, MyShape temperShape, boolean fromCenter) throws RemoteException;

    public void painted(String name, MyShape durableShape, boolean fromCenter) throws RemoteException;

    public boolean enterRequest(String applierName, IRemoteWhiteBoard rwb) throws RemoteException;

    public void acceptEnterRequest() throws RemoteException;

    public void kickRWB() throws RemoteException;

    public void quitEnterRequest(String applierName) throws RemoteException;

    public void dropoutRequest(String applierName) throws RemoteException;

    public void exitCurrentWhiteBoard() throws RemoteException;

    public String getRWBName() throws RemoteException;

    public String getOwnerName() throws RemoteException;

    public void addConnectedUser(String name) throws RemoteException;

    public void rmConnectedUser(String name) throws RemoteException;

    public void clearWhiteBoard() throws RemoteException;

    public void sendMessage(String ownerName, String message, boolean fromCenter) throws RemoteException;
}
