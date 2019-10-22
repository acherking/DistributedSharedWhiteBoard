package remote;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IRemoteCenterServer extends Remote {

    public boolean bindUserName(String userName) throws RemoteException;

    public void unbindUserName(String userName) throws RemoteException;

    public String[] getRWBList() throws RemoteException;

    // return null if there fullRWBName not exist
    public IRemoteWhiteBoard getRWB(String fullRWBName) throws RemoteException;

    public boolean addRWB(String ownerName, String rwbName, IRemoteWhiteBoard rwb) throws RemoteException;

    public void removeRWB(String ownerName, String rwbName) throws RemoteException;
}
