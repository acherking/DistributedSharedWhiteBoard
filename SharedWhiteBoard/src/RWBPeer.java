import remote.IRemoteWhiteBoard;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Vector;

public class RWBPeer {
    private Vector<IRemoteWhiteBoard> RWBsList;
    private JFrame mainJFrame;
    private JTextField inputRWBName;
    private JTextField inputHostName;
    private JTextField inputUserName;
    private JLabel statusLabel;
    private Registry localRegistry;
    private String localHostName;

    public RWBPeer() {
        RWBsList = new Vector<>();
        try {
            localRegistry = LocateRegistry.getRegistry("localhost");
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        try {
            localHostName = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        prepareGUI();
    }

    private void prepareGUI() {
        mainJFrame = new JFrame("RWBSinglePeer");
        mainJFrame.setSize(400, 500);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new FlowLayout());
        mainJFrame.add(mainPanel, BorderLayout.CENTER);

        statusLabel = new JLabel("",JLabel.CENTER);
        statusLabel.setSize(350,100);
        mainJFrame.add(statusLabel, BorderLayout.NORTH);

        mainJFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent){
                unbindAll();
                System.exit(0);
            }
        });

        Font inputRWBNameFont = new Font("Monaco", Font.PLAIN, 18);
        inputRWBName = new JTextField("White-Board Name", 22);
        inputRWBName.setFont(inputRWBNameFont);
        inputHostName = new JTextField("Remote HostName", 22);
        inputHostName.setFont(inputRWBNameFont);
        inputUserName = new JTextField("Login UserName", 22);
        inputUserName.setFont(inputRWBNameFont);
        mainPanel.add(inputHostName);
        mainPanel.add(inputRWBName);
        mainPanel.add(inputUserName);

        JButton createButton = new JButton("Create");
        JButton connectButton = new JButton("Connect");
        createButton.setActionCommand("Create");
        connectButton.setActionCommand("Connect");
        createButton.addActionListener(new ButtonClickListener());
        connectButton.addActionListener(new ButtonClickListener());
        mainPanel.add(createButton);
        mainPanel.add(connectButton);

        mainJFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent){
                System.exit(0);
            }
        });
    }

    private void showGUI() {
        mainJFrame.setVisible(true);
    }

    private void unbindAll() {
        try {
            String[] rwbNameList = localRegistry.list();
            for (int i=0; i<rwbNameList.length; i++) {
                localRegistry.unbind(rwbNameList[i]);
            }
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }

    public void setStateLabel(String str) {
        statusLabel.setText(str);
    }

    public String getInputUserName() {
        return inputUserName.getText();
    }

    public String getInputRWBName() {
        return inputRWBName.getText();
    }

    public String getInputHostName() {
        return inputHostName.getText();
    }

    public boolean verifyRWBName(String rwbName) {
        return true;
    }

    public static void main(String[] args) {
        RWBPeer peer = new RWBPeer();
        peer.showGUI();
    }

    private class ButtonClickListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals("Create")) {
                createNewRWB();
            }
            else if (command.equals("Connect")) {
                connectToExistRWB();
            }
            else {
                System.out.printf("In ButtonClickListener: unknown command [%s]\n", command);
            }
        }

        public void createNewRWB() {
            String rwbName = getInputRWBName();
            String userName = getInputUserName();
            if (verifyRWBName(rwbName)) {
                try {
                    RemoteWhiteBoard rwb = new RemoteWhiteBoard(userName, rwbName,true, null);
                    localRegistry.bind(userName+"-"+rwbName, rwb);
                    RWBsList.add(rwb);
                    rwb.showWhiteBoard();
                } catch (RemoteException | AlreadyBoundException e) {
                    e.printStackTrace();
                }
            }
            else {
                setStateLabel("Existed White-Board Name, please change another one.");
            }
        }

        public void connectToExistRWB() {
            String rwbName = getInputRWBName();
            String[] fullRWBName = rwbName.split("-");
            String managerName = fullRWBName[0];
            String centerRWBName = fullRWBName[1];
            String hostName = getInputHostName();
            String userName = getInputUserName();
            Registry remoteRegistry;

            try {
                remoteRegistry = LocateRegistry.getRegistry(hostName);
                IRemoteWhiteBoard centerRWB = (IRemoteWhiteBoard) remoteRegistry.lookup(rwbName);
                RemoteWhiteBoard rwb = new RemoteWhiteBoard(userName, centerRWBName, false, centerRWB);
                // localRegistry.bind(userName+"-"+centerRWBName, rwb);
                if (centerRWB.enterRequest(userName, rwb)) {
                    rwb.showWhiteBoard();
                }
                else {
                    localRegistry.unbind(userName+"-"+rwbName);
                }
            } catch (RemoteException | NotBoundException e) {
                e.printStackTrace();
            }
        }
    }
}
