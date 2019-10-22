import remote.IRemoteCenterServer;
import remote.IRemoteWhiteBoard;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;

public class RemoteCenterServer extends UnicastRemoteObject implements IRemoteCenterServer {
    private ConcurrentHashMap<String, IRemoteWhiteBoard> RWBsMap;
    private ConcurrentHashMap<String, Integer> boundNameMap;
    private JFrame baseJFrame;
    private JTextArea boundUserNameList;
    private JTextArea boundCenterRWBList;
    private JTextArea commandLog;
    Registry registry;

    public RemoteCenterServer() throws  RemoteException{
        RWBsMap = new ConcurrentHashMap<>();
        boundNameMap = new ConcurrentHashMap<>();
        prepareGUI();

        try {
            registry = LocateRegistry.getRegistry("localhost");
            registry.bind("RWB-Center-Server", this);
        } catch (RemoteException | AlreadyBoundException e) {
            e.printStackTrace();
        }
    }

    private void prepareGUI() {
        baseJFrame = new JFrame("RWB Center Server");
        baseJFrame.setSize(1000, 600);
        baseJFrame.setLayout(new FlowLayout());

        baseJFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent){
                try {
                    registry.unbind("RWB-Center-Server");
                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (NotBoundException e) {
                    e.printStackTrace();
                }
                System.exit(0);
            }
        });

        boundUserNameList = new JTextArea("User List:\n", 20, 24);
        boundCenterRWBList = new JTextArea("Center RWB List:\n", 20, 24);
        commandLog = new JTextArea("Command Log:\n", 20, 30);
        Font listFont = new Font("Courier", Font.BOLD,18);
        Font logFont = new Font("Monaco", Font.PLAIN, 17);
        boundUserNameList.setFont(listFont);
        boundCenterRWBList.setFont(listFont);
        commandLog.setFont(logFont);

        JScrollPane scrollNameList = new JScrollPane(boundUserNameList);
        JScrollPane scrollRWBList = new JScrollPane(boundCenterRWBList);
        JScrollPane scrollCommandLog = new JScrollPane(commandLog);

        baseJFrame.add(scrollNameList);
        baseJFrame.add(scrollRWBList);
        baseJFrame.add(scrollCommandLog);

    }

    public void showGUI() {
        baseJFrame.setVisible(true);
    }

    private void updateUserListField() {
        boundUserNameList.setText("User List:\n");

        for(String userName : boundNameMap.keySet()) {
            appendToUserListField(userName);
        }

        appendToCommandLog("Update User List Field.");
     }

    private void updateRWBsListField() {
        boundCenterRWBList.setText("Center RWB List:\n");

        for(String fullRWBName : RWBsMap.keySet()) {
            appendToRWBsListField(fullRWBName);
        }

        appendToCommandLog("Update RWB List Field.");
    }

    private void appendToUserListField(String userName) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                boundUserNameList.append(userName+"\n");
            }
        });
    }

    private void appendToRWBsListField(String fullRWBName) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                boundCenterRWBList.append(fullRWBName+"\n");
            }
        });
    }

    private void appendToCommandLog(String logStr) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                commandLog.append(logStr+"\n");
            }
        });
    }

    @Override
    public boolean bindUserName(String userName) throws RemoteException {
        appendToCommandLog("Bind user: ["+userName+"].");
        if(boundNameMap.containsKey(userName)) {
            return false;
        }
        else {
            boundNameMap.put(userName, 0);
            appendToUserListField(userName);
            return true;
        }
    }

    @Override
    public void unbindUserName(String userName) throws RemoteException {
        appendToCommandLog("Unbind user: ["+userName+"].");
        if(boundNameMap.containsKey(userName)) {
            boundNameMap.remove(userName);
            updateUserListField();
        }
    }

    @Override
    public String[] getRWBList() throws RemoteException {
        appendToCommandLog("Get RWB list.");
        String[] rwbFullNameStrList = new String[RWBsMap.size()];
        int i = 0;
        for(String rwbFullName : RWBsMap.keySet()) {
            rwbFullNameStrList[i] = rwbFullName;
            i = i + 1;
        }
        return rwbFullNameStrList;
    }

    @Override
    public IRemoteWhiteBoard getRWB(String fullRWBName) throws RemoteException{
        appendToCommandLog("Get RWB: ["+fullRWBName+"] .");
        if (RWBsMap.containsKey(fullRWBName)) {
            System.out.printf("In getRWb: contain [%s]\n", fullRWBName);
            return RWBsMap.get(fullRWBName);
        }
        else {
            return null;
        }
    }

    @Override
    public boolean addRWB(String ownerName, String rwbName, IRemoteWhiteBoard rwb) throws RemoteException {
        String fullRWBName = ownerName + "-" + rwbName;
        appendToCommandLog("Add RWB: ["+fullRWBName+"] .");
        if (RWBsMap.containsKey(fullRWBName)) {
            return false;
        }
        else {
            RWBsMap.put(fullRWBName, rwb);
            appendToRWBsListField(fullRWBName);
            return true;
        }
    }

    @Override
    public void removeRWB(String ownerName, String rwbName) throws RemoteException {
        String fullRWBName = ownerName + "-" + rwbName;
        appendToCommandLog("Remote RWB: ["+fullRWBName+"] .");
        if(RWBsMap.containsKey(fullRWBName)) {
            RWBsMap.remove(fullRWBName);
            updateRWBsListField();
        }
    }

    public static void main(String[] args) {
        try {
            RemoteCenterServer centerServer = new RemoteCenterServer();
            centerServer.showGUI();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
