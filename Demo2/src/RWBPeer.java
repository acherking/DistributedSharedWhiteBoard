import remote.IRemoteCenterServer;
import remote.IRemoteWhiteBoard;

import javax.swing.*;
import javax.swing.border.Border;
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
    private Vector<RemoteWhiteBoard> RWBsList;
    private JFrame inputNameJFrame;
    private JFrame connectRWBJFrame;
    private JFrame createRWBJFrame;
    private JTextField inputRWBName;
    private JList<String> centerRWBListField;
    private JTextField inputUserName;
    private JLabel namePageStateLabel, connectPageStateLabel, createPageStateLabel;
    private Registry localRegistry;
    private IRemoteCenterServer centerServer;
    private String localHostName;
    private String boundUserName;

    public RWBPeer(String remoteHostName) {
        RWBsList = new Vector<>();
        boundUserName = null;
        try {
            localRegistry = LocateRegistry.getRegistry("localhost");
            Registry remoteRegistry = LocateRegistry.getRegistry(remoteHostName);
            centerServer = (IRemoteCenterServer) remoteRegistry.lookup("RWB-Center-Server");
        } catch (RemoteException | NotBoundException e) {
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
        Font inputRWBNameFont = new Font("Monaco", Font.PLAIN, 18);

        // for input name
        inputNameJFrame = new JFrame("RWBSinglePeer: Get-UserName");
        inputNameJFrame.setSize(200, 250);
        inputNameJFrame.setLayout(new BorderLayout());

        inputUserName = new JTextField("Login UserName", 22);
        inputUserName.setFont(inputRWBNameFont);
        JButton bindNameButton = new JButton("Bind Name");
        bindNameButton.setActionCommand("bind-userName");
        bindNameButton.addActionListener(new ButtonClickListener());

        JPanel namePagePanel = new JPanel();
        namePagePanel.setLayout(new FlowLayout());
        namePagePanel.add(inputUserName);
        namePagePanel.add(bindNameButton);

        namePageStateLabel = new JLabel("",JLabel.CENTER);
        namePageStateLabel.setSize(350,100);

        inputNameJFrame.add(namePageStateLabel, BorderLayout.NORTH);
        inputNameJFrame.add(namePagePanel, BorderLayout.CENTER);

        inputNameJFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent){
                System.exit(0);
            }
        });

        // for connect to exist RWB
        connectRWBJFrame = new JFrame("RWBSinglePeer: Connect-RWB");
        connectRWBJFrame.setSize(400, 500);
        connectRWBJFrame.setLayout(new BorderLayout());

        try {
            centerRWBListField = new JList(centerServer.getRWBList()); //data has type Object[]
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        centerRWBListField.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        centerRWBListField.setLayoutOrientation(JList.VERTICAL);
        centerRWBListField.setVisibleRowCount(-1);
        JScrollPane listScroller = new JScrollPane(centerRWBListField);
        listScroller.setPreferredSize(new Dimension(250, 80));

        JPanel connectPageButtonPanel = new JPanel();
        connectPageButtonPanel.setLayout(new FlowLayout());
        JButton refreshRWBListButton = new JButton("Refresh");
        refreshRWBListButton.setActionCommand("refresh");
        refreshRWBListButton.addActionListener(new ButtonClickListener());
        JButton connectToRWBButton = new JButton("Connect");
        connectToRWBButton.setActionCommand("connect");
        connectToRWBButton.addActionListener(new ButtonClickListener());
        JButton createNewRWBButton = new JButton("Create");
        createNewRWBButton.setActionCommand("open_create");
        createNewRWBButton.addActionListener(new ButtonClickListener());
        connectPageButtonPanel.add(refreshRWBListButton);
        connectPageButtonPanel.add(connectToRWBButton);
        connectPageButtonPanel.add(createNewRWBButton);

        connectPageStateLabel = new JLabel("",JLabel.CENTER);
        connectPageStateLabel.setSize(350,100);

        connectRWBJFrame.add(connectPageStateLabel, BorderLayout.NORTH);
        connectRWBJFrame.add(listScroller, BorderLayout.CENTER);
        connectRWBJFrame.add(connectPageButtonPanel, BorderLayout.SOUTH);

        connectRWBJFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent){
                // unbindAll();
                try {
                    // wzx: remove all created RWB in centerServer
                    for(RemoteWhiteBoard rwb : RWBsList) {
                        centerServer.removeRWB(boundUserName, rwb.getRWBName());
                        rwb.exitWhiteBoard();
                    }
                    centerServer.unbindUserName(boundUserName);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                System.exit(0);
            }
        });

        // for create new RWB
        createRWBJFrame = new JFrame("RWBSinglePeer: Create-RWB");
        createRWBJFrame.setSize(200, 250);
        createRWBJFrame.setLayout(new BorderLayout());

        inputRWBName = new JTextField("White-Board Name", 22);
        inputRWBName.setFont(inputRWBNameFont);

        JButton createButton = new JButton("Create");
        createButton.setActionCommand("create");
        createButton.addActionListener(new ButtonClickListener());

        JPanel createPagePanel = new JPanel();
        createPagePanel.setLayout(new FlowLayout());
        createPagePanel.add(inputRWBName);
        createPagePanel.add(createButton);

        createPageStateLabel = new JLabel("",JLabel.CENTER);
        createPageStateLabel.setSize(350,100);

        createRWBJFrame.add(createPageStateLabel, BorderLayout.NORTH);
        createRWBJFrame.add(createPagePanel, BorderLayout.CENTER);

        createRWBJFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent){
                createRWBJFrame.setVisible(false);
            }
        });
    }

    private void showGUI() {
        inputNameJFrame.setVisible(true);
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

    public void setNamePageStateLabel(String str) {
        namePageStateLabel.setText(str);
    }

    public void setConnectPageStateLabel(String str) {
        connectPageStateLabel.setText(str);
    }

    public void setCreatePageStateLabel(String str) {
        createPageStateLabel.setText(str);
    }

    public String getInputUserName() {
        return inputUserName.getText();
    }

    public String getInputRWBName() {
        return inputRWBName.getText();
    }

    public boolean verifyRWBName(String rwbName) {
        return true;
    }

    // args[0]:remoteHostName
    public static void main(String[] args) {
        String hostname = "localhost";
        RWBPeer peer = new RWBPeer(hostname);
        peer.showGUI();
    }

    private class ButtonClickListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals("create")) {
                createNewRWB();
            }
            else if (command.equals("connect")) {
                connectToExistRWB();
            }
            else if (command.equals("open_create")) {
                createRWBJFrame.setVisible(true);
            }
            else if (command.equals("refresh")) {
                refreshCenterRWBField();
            }
            else if(command.equals("bind-userName")) {
                bindUserName();
            }
            else {
                System.out.printf("In ButtonClickListener: unknown command [%s]\n", command);
            }
        }

        public boolean validInputStr(String str) {
            if(str != null && !str.isEmpty()) return true;
            return false;
        }

        public void bindUserName() {
            String userName = getInputUserName();
            if (validInputStr(userName)){
                try {
                    if (centerServer.bindUserName(userName)) {
                        boundUserName = userName;
                        connectRWBJFrame.setTitle("RWB-Peer-"+userName);
                        connectRWBJFrame.setVisible(true);
                        inputNameJFrame.setVisible(false);
                        inputNameJFrame.dispose();
                    }
                    else {
                        setNamePageStateLabel("UserName already exist.");
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            else {
                setNamePageStateLabel("Invalid UserName.");
            }
        }

        public void refreshCenterRWBField() {
            try {
                centerRWBListField.setListData(centerServer.getRWBList());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        public void connectToExistRWB() {
            String centerRWBFullName = centerRWBListField.getSelectedValue();
            System.out.printf("In connectToExistRWB: select centerRWB is [%s]\n", centerRWBFullName);

            // can not connect to self
            String userName = centerRWBFullName.split("-")[0];
            if (userName.equals(boundUserName)) {
                setConnectPageStateLabel("Can not connect to self creat whiteboard.");
                return;
            }

            try {
                IRemoteWhiteBoard centerRWB = centerServer.getRWB(centerRWBFullName);
                if (centerRWB != null) {
                    String rwbName = centerRWBFullName.split("-")[1];
                    RemoteWhiteBoard rwb = new RemoteWhiteBoard(boundUserName, rwbName, false, centerRWB, null);
                    // localRegistry.bind(boundUserName+"-"+rwbName, rwb);
                    if (centerRWB.enterRequest(boundUserName, rwb)) {
                        rwb.setInfoJFrame("Waiting acceptance.");
                        rwb.showInfoJFrame();
                    }
                    else {
                        setConnectPageStateLabel("Can not connect to same whiteboard.");
                        //localRegistry.unbind(boundUserName+"-"+rwbName);
                    }
                }
                else {
                    setConnectPageStateLabel("Connect failed, please refresh.");
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        public void createNewRWB() {
            System.out.printf("In createNewRWB: create new RWB\n");
            String rwbName = getInputRWBName();
            if (!validInputStr(rwbName)) {
                setCreatePageStateLabel("Invalid input Remote WhiteBoard Name.");
                return;
            }

            try {
                RemoteWhiteBoard rwb = new RemoteWhiteBoard(boundUserName, rwbName,true, null, centerServer);
                if (centerServer.addRWB(boundUserName, rwbName, rwb)) {
                    RWBsList.add(rwb);
                    // localRegistry.bind(boundUserName+"-"+rwbName, rwb);
                    createRWBJFrame.setVisible(false);
                    rwb.showWhiteBoard();
                }
                else {
                    setCreatePageStateLabel("Existed Whiteboard Name, please change another one.");
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }

    }
}
