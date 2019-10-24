import remote.IRemoteCenterServer;
import remote.IRemoteWhiteBoard;
import remote.MyShape;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.*;
import java.io.*;
import java.net.URLDecoder;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class RemoteWhiteBoard extends UnicastRemoteObject implements IRemoteWhiteBoard {
    private JFrame informJFrame;
    private JLabel informLabel;

    private JFrame baseJFrame;
    // for File menu bar in whiteBoardMenu
    private JMenu myFileMenu;
    private WhiteBoardMenu whiteBoardMenu;
    private UserManagerPanel userManagerPanel;
    private ChattingWindowPanel chattingWindowPanel;
    private PaintSurface paintSurface;
    private String userName;
    private String rwbName;
    private boolean isCenter;
    private boolean waitingAccept;
    private IRemoteWhiteBoard centerWhiteBoard;
    private IRemoteCenterServer centerServer;
    private RWBPeer myPeer;
    // for center whiteboard to synchronize data(shape, message)
    private ConcurrentHashMap<String, IRemoteWhiteBoard> connectedWhiteBoards;
    private ConcurrentHashMap<String, IRemoteWhiteBoard> requestWhiteBoards;

    private JTextField fileNameText;
    private JFrame saveJFrame = new JFrame("Save");
    private JFrame saveResultJFrame;
    private JFrame openJFrame;
    private JFrame saveAsJFrame = new JFrame("Save-As");


    public RemoteWhiteBoard() throws RemoteException {
        this.userName = "initName";
        this.rwbName = "initRWBName";
        this.isCenter = false;
        this.waitingAccept = true;
        this.centerWhiteBoard = null;
        this.connectedWhiteBoards = null;
        this.requestWhiteBoards = null;
    }

    public RemoteWhiteBoard(String userName, String rwbName, boolean isCenter,
                            IRemoteWhiteBoard centerRWB, IRemoteCenterServer centerServer, RWBPeer peer) throws RemoteException {
        this();
        this.userName = userName;
        this.rwbName = rwbName;
        this.isCenter = isCenter;
        this.myPeer = peer;

        if (this.isCenter)  {
            this.waitingAccept = false;
            connectedWhiteBoards = new ConcurrentHashMap<>();
            requestWhiteBoards = new ConcurrentHashMap<>();
            this.centerServer = centerServer;
        }
        else {
            this.centerWhiteBoard = centerRWB;
            this.centerServer = null;
        }

        // init informJFrame
        informJFrame = new JFrame(userName+"-"+rwbName);
        informJFrame.setBounds(100, 100, 400, 200);
        informJFrame.setLayout(new BorderLayout());
        informLabel = new JLabel("Init Inform", JLabel.CENTER);
        informLabel.setFont(new Font("Monaco", Font.PLAIN, 26));
        informJFrame.add(informLabel, BorderLayout.CENTER);
        informJFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent){
                quitEnterRequest();
            }
        });

        // init baseJFrame
        baseJFrame = new JFrame();
        baseJFrame.setTitle(userName+"-"+rwbName);
        //baseJFrame.setBounds(100, 100, 1000, 700);
        baseJFrame.setSize(1100,800);
        //baseJFrame.setLayout(new FlowLayout());
        baseJFrame.setLayout(new BorderLayout());

        baseJFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent){
                exitWhiteBoard();
            }
        });

        paintSurface = new PaintSurface();
        // wzx: what this mean, why do it
        //paintSurface.setBounds(300,300, 312, 342);
        paintSurface.setSize(500,500);
        baseJFrame.add(paintSurface,BorderLayout.CENTER);
        //baseJFrame.add(paintSurface);

        userManagerPanel = new UserManagerPanel();
        if (isCenter) {
            userManagerPanel.addConnectedUser(userName+"(Admin)-self");
        }
        else {
            String adminName = centerRWB.getOwnerName();
            userManagerPanel.addConnectedUser(adminName+"(Admin)");
            userManagerPanel.addConnectedUser(userName+"-self");
        }

        chattingWindowPanel = new ChattingWindowPanel();

        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BorderLayout());
        sidePanel.add(userManagerPanel, BorderLayout.NORTH);
        sidePanel.add(chattingWindowPanel, BorderLayout.CENTER);
        baseJFrame.add(sidePanel, BorderLayout.EAST);

        initMyFileMenu();
        whiteBoardMenu = new WhiteBoardMenu(myFileMenu, this.isCenter);


        baseJFrame.setJMenuBar(whiteBoardMenu);
    }

    public void showWhiteBoard() {
        baseJFrame.setVisible(true);
    }

    public void showInfoJFrame() {informJFrame.setVisible(true);}

    public void setInfoJFrame(String info) {informLabel.setText(info);}

    public String getUserName() {
        return userName;
    }

    public void exitWhiteBoard() {
        if (isCenter) {
            try {
                this.centerServer.removeRWB(userName, rwbName);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            exitAllRWB();
            try {
                exitCurrentWhiteBoard();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        else {
            try {
                centerWhiteBoard.dropoutRequest(userName);
                exitCurrentWhiteBoard();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void exitAllRWB() {
        // exit connected RWB
        Set<Map.Entry<String, IRemoteWhiteBoard>> entrySet = connectedWhiteBoards.entrySet();
        Iterator<Map.Entry<String, IRemoteWhiteBoard>> itr = entrySet.iterator();
        while (itr.hasNext()) {
            Map.Entry<String, IRemoteWhiteBoard> entry = itr.next();
            String rOwnerName = entry.getKey();
            IRemoteWhiteBoard rwb = entry.getValue();
            try {
                rwb.exitCurrentWhiteBoard();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        // cancel all request RWB
    }

    private void initMyFileMenu() {
        myFileMenu = new JMenu("File");
        // myFileMenu.setFont(new Font("Georgia", Font.PLAIN, 12));

        JMenuItem mntmOpen = new JMenuItem("Open");
        mntmOpen.setFont(new Font("Georgia", Font.PLAIN, 12));
        mntmOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
        myFileMenu.add(mntmOpen);
        mntmOpen.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                open();
            }
        });


        JMenuItem mntmSave = new JMenuItem("Save");
        mntmSave.setFont(new Font("Georgia", Font.PLAIN, 12));
        mntmSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
        myFileMenu.add(mntmSave);
        mntmSave.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                save();
            }
        });


        JMenuItem mntmSaveAs = new JMenuItem("Save As");
        mntmSaveAs.setFont(new Font("Georgia", Font.PLAIN, 12));
        mntmSaveAs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK));
        myFileMenu.add(mntmSaveAs);
        mntmSaveAs.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveas();
            }
        });

        JMenuItem mntmClose = new JMenuItem("Close");
        mntmClose.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK));
        mntmClose.setFont(new Font("Georgia", Font.PLAIN, 12));
        myFileMenu.add(mntmClose);
        mntmClose.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                exitWhiteBoard();
            }
        });
    }

    @Override
    public void painting(String name, MyShape temperShape, boolean fromCenter) throws RemoteException {
        paintSurface.addTemperShape(name, temperShape, fromCenter);
        paintSurface.repaint();
    }

    @Override
    public void painted(String name, MyShape durableShape, boolean fromCenter) throws RemoteException {
        paintSurface.addDurableShape(name, durableShape, fromCenter);
        paintSurface.repaint();
    }

    @Override
    public boolean enterRequest(String applierName, IRemoteWhiteBoard rwb) throws RemoteException {
        if (connectedWhiteBoards.containsKey(applierName) || requestWhiteBoards.containsKey(applierName)) {
            return false;
        }
        else {
            requestWhiteBoards.put(applierName, rwb);
            userManagerPanel.addRequestUser(applierName);
            return true;
        }
    }

    @Override
    public void acceptEnterRequest() throws RemoteException {
        informJFrame.setVisible(false);
        this.waitingAccept = false;
        this.showWhiteBoard();
    }

    @Override
    public void kickRWB() throws RemoteException {
        this.exitCurrentWhiteBoard();
        setInfoJFrame("You have been kicked out of the Shared Whiteboard.");
        this.showInfoJFrame();
    }

    @Override
    public void quitEnterRequest(String applierName) throws RemoteException {
        userManagerPanel.removeRequestUser(applierName);
    }

    public void quitEnterRequest() {
        if (this.waitingAccept) {
            try {
                this.centerWhiteBoard.quitEnterRequest(userName);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void dropoutRequest(String applierName) throws RemoteException {
        removeRemoteWhiteBoard(applierName);
        userManagerPanel.removeConnectedUser(applierName);
        removeTheCUFromAllRWBs(applierName);
    }

    @Override
    public void exitCurrentWhiteBoard() throws RemoteException {
        baseJFrame.setVisible(false);
        /*
        Registry localRegistry = LocateRegistry.getRegistry("localhost");
        try {
            localRegistry.unbind(userName+"-"+rwbName);
        } catch (NotBoundException e) {
            e.printStackTrace();
        }*/
        myPeer.rmMyRWB(this);
        baseJFrame.dispose();
        informJFrame.dispose();
    }

    @Override
    public String getRWBName() throws RemoteException {
        return rwbName;
    }

    @Override
    public String getOwnerName() throws RemoteException {
        return userName;
    }

    @Override
    public void addConnectedUser(String name) throws RemoteException {
        userManagerPanel.addConnectedUser(name);
    }

    @Override
    public void rmConnectedUser(String name) throws RemoteException {
        userManagerPanel.removeConnectedUser(name);
    }

    @Override
    public void clearWhiteBoard() throws RemoteException {
        paintSurface.clearAllShapes();
    }

    @Override
    public void sendMessage(String ownerName, String message, boolean fromCenter) throws RemoteException {
        chattingWindowPanel.appendToChatBox(ownerName, message);
        if(!fromCenter) {
            if (isCenter) {
                Set<Map.Entry<String, IRemoteWhiteBoard>> entrySet = connectedWhiteBoards.entrySet();
                Iterator<Map.Entry<String, IRemoteWhiteBoard>> itr = entrySet.iterator();
                while (itr.hasNext()) {
                    Map.Entry<String, IRemoteWhiteBoard> entry = itr.next();
                    String targetName = entry.getKey();
                    if (!targetName.equals(ownerName)) {
                        IRemoteWhiteBoard rwb = entry.getValue();
                        rwb.sendMessage(ownerName, message, true);
                    }
                }
            }
        }
    }

    // for center
    public void addRemoteWhiteBoard(String name, IRemoteWhiteBoard rwb) {
        connectedWhiteBoards.put(name, rwb);
    }

    public void removeRemoteWhiteBoard(String name) {
        connectedWhiteBoards.remove(name);
    }

    public void synConnectedUserField(String applierName, IRemoteWhiteBoard rwb) {
        for(String name : connectedWhiteBoards.keySet()) {
            if(!name.equals(applierName)) {
                try {
                    rwb.addConnectedUser(name);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void removeTheCUFromAllRWBs(String userName) {
        Set<Map.Entry<String, IRemoteWhiteBoard>> entrySet = connectedWhiteBoards.entrySet();
        Iterator<Map.Entry<String, IRemoteWhiteBoard>> itr = entrySet.iterator();
        while (itr.hasNext()) {
            Map.Entry<String, IRemoteWhiteBoard> entry = itr.next();
            IRemoteWhiteBoard rwb = entry.getValue();
            try {
                rwb.rmConnectedUser(userName);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void addTheCUToAllRWBs(String userName) {
        Set<Map.Entry<String, IRemoteWhiteBoard>> entrySet = connectedWhiteBoards.entrySet();
        Iterator<Map.Entry<String, IRemoteWhiteBoard>> itr = entrySet.iterator();
        while (itr.hasNext()) {
            Map.Entry<String, IRemoteWhiteBoard> entry = itr.next();
            String ownerName = entry.getKey();
            if(!ownerName.equals(userName)) {
                IRemoteWhiteBoard rwb = entry.getValue();
                try {
                    rwb.addConnectedUser(userName);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void rmTheCUToAllRWBs(String userName) {
        Set<Map.Entry<String, IRemoteWhiteBoard>> entrySet = connectedWhiteBoards.entrySet();
        Iterator<Map.Entry<String, IRemoteWhiteBoard>> itr = entrySet.iterator();
        while (itr.hasNext()) {
            Map.Entry<String, IRemoteWhiteBoard> entry = itr.next();
            String ownerName = entry.getKey();
            if(!ownerName.equals(userName)) {
                IRemoteWhiteBoard rwb = entry.getValue();
                try {
                    rwb.rmConnectedUser(userName);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class UserManagerPanel extends JPanel {
        JList<String> connectedUserJList;
        JList<String> requestUserJList;
        DefaultListModel<String> cUlistModel;
        DefaultListModel<String> rUlistModel;

        public UserManagerPanel() {
            this.setLayout(new FlowLayout());
            //this.setLayout(new BorderLayout());
            JPanel connectedUJPanel = new JPanel();
            cUlistModel = new DefaultListModel<>();
            connectedUserJList = new JList<>(cUlistModel);
            connectedUserJList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
            connectedUserJList.setLayoutOrientation(JList.VERTICAL);
            connectedUserJList.setVisibleRowCount(-1);
            JScrollPane cListScroller = new JScrollPane(connectedUserJList);
            cListScroller.setPreferredSize(new Dimension(120, 100));
            JButton kickButton = new JButton("Kick");
            kickButton.setActionCommand("kick");
            kickButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String kickUserName = connectedUserJList.getSelectedValue();
                    if (kickUserName!=null) {
                        if(kickUserName.split("-").length==1) {
                            try {
                                IRemoteWhiteBoard rwb = connectedWhiteBoards.get(kickUserName);
                                connectedWhiteBoards.remove(kickUserName);
                                removeConnectedUser(kickUserName);
                                rmTheCUToAllRWBs(kickUserName);
                                rwb.kickRWB();
                            } catch (RemoteException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
            });
            connectedUJPanel.setLayout(new BorderLayout());
            connectedUJPanel.add(cListScroller, BorderLayout.CENTER);
            if (isCenter) {
                connectedUJPanel.add(kickButton, BorderLayout.SOUTH);
            }
            this.add(connectedUJPanel);

            if (isCenter) {
                JPanel requestUJPanel = new JPanel();
                rUlistModel = new DefaultListModel<>();
                requestUserJList = new JList<>(rUlistModel);
                requestUserJList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
                requestUserJList.setLayoutOrientation(JList.VERTICAL);
                requestUserJList.setVisibleRowCount(-1);
                JScrollPane rListScroller = new JScrollPane(requestUserJList);
                rListScroller.setPreferredSize(new Dimension(120, 100));
                JButton acceptButton = new JButton("Accept");
                acceptButton.setActionCommand("accept");
                acceptButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String acceptUserName = requestUserJList.getSelectedValue();
                        if (acceptUserName!=null) {
                            try {
                                IRemoteWhiteBoard rwb = requestWhiteBoards.get(acceptUserName);
                                requestWhiteBoards.remove(acceptUserName);
                                paintSurface.synDurableShapesTo(rwb);
                                connectedWhiteBoards.put(acceptUserName, rwb);
                                synConnectedUserField(acceptUserName,rwb);
                                rwb.acceptEnterRequest();
                                addTheCUToAllRWBs(acceptUserName);
                                removeRequestUser(acceptUserName);
                                addConnectedUser(acceptUserName);
                            } catch (RemoteException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                });
                requestUJPanel.setLayout(new BorderLayout());
                requestUJPanel.add(rListScroller, BorderLayout.CENTER);
                requestUJPanel.add(acceptButton, BorderLayout.SOUTH);
                this.add(requestUJPanel);
            }
        }

        public void addConnectedUser(String newUserName) {
            cUlistModel.addElement(newUserName);
        }

        public void removeConnectedUser(String rmUserName) {
            cUlistModel.removeElement(rmUserName);
        }

        public void addRequestUser(String requestUserName) {
            rUlistModel.addElement(requestUserName);
        }

        public void removeRequestUser(String rmRuserName) {
            rUlistModel.removeElement(rmRuserName);
        }
    }

    private class ChattingWindowPanel extends JPanel {
        private JTextArea chattingBox;
        private JTextArea inputMessage;

        public ChattingWindowPanel() {
            this.setLayout(new BorderLayout());
            chattingBox = new JTextArea(50, 30);
            JScrollPane cScroll = new JScrollPane(chattingBox);
            cScroll.setPreferredSize(new Dimension(240, 200));
            JPanel mPanel = new JPanel();
            inputMessage = new JTextArea(3, 20);
            JScrollPane mScroll = new JScrollPane(inputMessage);
            mScroll.setPreferredSize(new Dimension(200, 40));
            JButton sendButton = new JButton("Send");
            sendButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String message = inputMessage.getText();
                    if(message!=null && (!message.isEmpty())) {
                        if (isCenter) {
                            try {
                                inputMessage.setText("");
                                sendMessage(userName, message, false);
                            } catch (RemoteException ex) {
                                ex.printStackTrace();
                            }
                        } else {
                            inputMessage.setText("");
                            try {
                                appendToChatBox(userName, message);
                                centerWhiteBoard.sendMessage(userName, message, false);
                            } catch (RemoteException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
            });
            mPanel.setLayout(new FlowLayout());
            mPanel.add(inputMessage);
            mPanel.add(sendButton);

            this.add(cScroll, BorderLayout.CENTER);
            this.add(mPanel, BorderLayout.SOUTH);
        }

        public void appendToChatBox(String name, String message) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    chattingBox.append(name+":"+message+"\n");
                    chattingBox.setCaretPosition(chattingBox.getDocument().getLength());
                }
            });
        }
    }

    protected class PaintSurface extends JPanel {
        private CopyOnWriteArrayList<MyShape> shapes = new CopyOnWriteArrayList<>();
        private ConcurrentHashMap<String, MyShape> temperShapes = new ConcurrentHashMap<>();
        Point startDrag, endDrag;
        Path2D path;
        Point inputPosition;
        String inputString;
        Shape inputStringBound;
        // wzx: what is fileName used for
        private String fileName;

        private class MyDispatcher implements KeyEventDispatcher {
            private Color textColor = Color.black;
            private float textB = 2;
            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                //wzx: test output
                System.out.printf("In mydispatcher, keycode=%d\n", e.getKeyCode());
                if (e.getID() == KeyEvent.KEY_PRESSED) {
                    if (whiteBoardMenu.getCurrentShape().equals("Text") && inputPosition!=null && inputString!=null) {
                        char inputChar = e.getKeyChar();
                        if(inputChar>='a' && inputChar<='z') {
                            inputString = inputString + inputChar;
                            Shape s = makeTextLayout();
                            addTemperShape(userName, new MyShape(s, textColor, false, textB), false);
                            inputStringBound = s.getBounds();
                            repaint();
                        }
                        else if(e.getKeyCode()==KeyEvent.VK_ENTER) {
                            Shape s = makeTextLayout();
                            addDurableShape(userName, new MyShape(s, textColor, false, textB), false);
                            inputPosition = null;
                            inputString = null;
                            inputStringBound = null;
                            repaint();
                        }
                        else if(e.getKeyCode()==8) {
                            if (inputString.length()>0) {
                                inputString = inputString.substring(0, inputString.length() - 1);
                                if (inputString.length()>0) {
                                    Shape s = makeTextLayout();
                                    addTemperShape(userName, new MyShape(s, textColor, false, textB), false);
                                    inputStringBound = s.getBounds();
                                }
                                else {
                                    temperShapes.remove(userName);
                                    inputStringBound = null;
                                }
                                repaint();
                            }
                        }
                        else {
                            // wzx: what is this
                            ;
                        }
                    }
                } else if (e.getID() == KeyEvent.KEY_RELEASED) {
                    ;
                } else if (e.getID() == KeyEvent.KEY_TYPED) {
                    ;
                }
                return false;
            }
        }

        public PaintSurface() {
            inputPosition = null;
            inputString = null;

            KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
            manager.addKeyEventDispatcher(new MyDispatcher());

            this.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    if(whiteBoardMenu.getCurrentShape().equals("Text")) {
                        temperShapes.remove(userName);
                        inputStringBound = makeRectangle(e.getX(),e.getY(),e.getX()+30, e.getY()+12);
                        repaint();
                        inputString = new String();
                        inputPosition = new Point(e.getX(), e.getY());
                        return;
                    }
                    startDrag = new Point(e.getX(), e.getY());
                    endDrag = startDrag;
                    path = new Path2D.Float();
                    path.moveTo(e.getX(), e.getY());
                    repaint();
                }
                public void mouseReleased(MouseEvent e) {
                    if(whiteBoardMenu.getCurrentShape().equals("Text")) {
                        return;
                    }
                    addDurableShape(userName, createNewShape(), false);
                    startDrag = null;
                    endDrag = null;
                    path = null;
                    repaint();
                }
            });


            this.addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseDragged(MouseEvent e) {
                    if(whiteBoardMenu.getCurrentShape().equals("Text")) {
                        return;
                    }
                    endDrag = new Point(e.getX(), e.getY());
                    path.lineTo(e.getX(), e.getY());
                    if(whiteBoardMenu.getCurrentShape().equals("EraserS")||
                            whiteBoardMenu.getCurrentShape().equals("EraserL")||
                            whiteBoardMenu.getCurrentShape().equals("EraserM")) {
                        addDurableShape(userName, createNewShape(), false);
                    }else {
                        addTemperShape(userName, createNewShape(), false);
                    }
                    repaint();
                }
            });
        }

        public void synDurableShapesTo(IRemoteWhiteBoard rwb) {
            for(MyShape myShape : shapes) {
                try {
                    rwb.painted("ForSynDurableShapes", myShape, true);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        public void synDurableShapesToAll() {
            for(IRemoteWhiteBoard rwb : connectedWhiteBoards.values()) {
                try {
                    rwb.clearWhiteBoard();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                synDurableShapesTo(rwb);

            }
        }

        public void clearAllShapes() {
            shapes.clear();
            temperShapes.clear();
            repaint();
        }

        private void paintBackground(Graphics2D g2){
            /*g2.setPaint(Color.LIGHT_GRAY);
            for (int i = 0; i < getSize().width; i += 10) {
                Shape line = new Line2D.Float(i, 0, i, getSize().height);
                g2.draw(line);
            }

            for (int i = 0; i < getSize().height; i += 10) {
                Shape line = new Line2D.Float(0, i, getSize().width, i);
                g2.draw(line);
            }*/
        	setOpaque(true);
        	setBackground(Color.WHITE);

        }
        public void paint(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            paintBackground(g2);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setStroke(new BasicStroke(2));
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.50f));

            for (MyShape sh : shapes) {
                g2.setStroke(new BasicStroke(sh.getBasicStroke()));
                g2.setPaint(sh.getColor());
                if(sh.getIsFill()==true) {
                    g2.fill(sh.getShape());
                }else {
                    g2.draw(sh.getShape());
                }
            }

            Set<Map.Entry<String, MyShape>> entrySet = temperShapes.entrySet();
            Iterator<Map.Entry<String, MyShape>> itr = entrySet.iterator();
            while (itr.hasNext()) {
                Map.Entry<String, MyShape> entry = itr.next();
                String ownerName = entry.getKey();
                Shape shape = entry.getValue().getShape();
                g2.setPaint(entry.getValue().getColor());
                if(entry.getValue().getIsFill()==true) {
                    g2.fill(shape);
                }else {
                    g2.draw(shape);
                }
            }

            if (inputStringBound!=null) {
                g2.setStroke(new BasicStroke(2));
                g2.setPaint(Color.black);
                g2.draw(inputStringBound);
            }

        }

        private MyShape createNewShape() {
            Shape r = null;
            boolean b=false;
            float bs=2;
            switch (whiteBoardMenu.getCurrentShape()) {
                case "Rectangle":
                    r = makeRectangle(startDrag.x, startDrag.y, endDrag.x, endDrag.y);
                    break;
                case "RectangleF":
                    b = true;
                    r = makeRectangle(startDrag.x, startDrag.y, endDrag.x, endDrag.y);
                    break;
                case "Oval":
                    r = makeEllipse(startDrag.x, startDrag.y, endDrag.x, endDrag.y);
                    break;
                case "OvalF":
                    b = true;
                    r = makeEllipse(startDrag.x, startDrag.y, endDrag.x, endDrag.y);
                    break;
                case "Circle":
                    r = makeCircle(startDrag.x, startDrag.y, endDrag.x, endDrag.y);
                    break;
                case "CircleF":
                    b = true;
                    r = makeCircle(startDrag.x, startDrag.y, endDrag.x, endDrag.y);
                    break;
                case "Line":
                    r = makeLine(startDrag.x, startDrag.y, endDrag.x, endDrag.y);
                    break;
                case "Painting Brush":
                    bs=10;
                    r = (Shape) path.clone();
                    break;
                case "Draw Pen":
                    r = (Shape) path.clone();
                    break;
                case "EraserS":
                    whiteBoardMenu.setCurrentColor(Color.white );
                    bs=6;
                    r = (Shape) path.clone();
                    break;
                case "EraserM":
                    whiteBoardMenu.setCurrentColor(Color.white );
                    bs=12;
                    r = (Shape) path.clone();
                    break;
                case "EraserL":
                    whiteBoardMenu.setCurrentColor(Color.white );
                    bs=18;
                    r = (Shape) path.clone();
                    break;
                case "Text":
                    r = (Shape) path.clone();
                    break;
                default:
                    System.out.printf("In PaintSurface.createNewShape: unknown currentState [%s]\n",
                            whiteBoardMenu.getCurrentShape());
                    break;
            }
            return new MyShape(r,whiteBoardMenu.getCurrentColor() ,b,bs);
        }

        public void addTemperShape(String ownerName, MyShape s, boolean fromCenter) {
            temperShapes.put(ownerName, s);
            if (!fromCenter) {
                addShapeToRWB(ownerName, s, false);
            }
        }

        public void addDurableShape(String ownerName, MyShape s, boolean fromCenter) {
            temperShapes.remove(ownerName);
            shapes.add(s);
            if (!fromCenter) {
                addShapeToRWB(ownerName, s, true);
            }
        }

        public void addShapeToRWB(String ownerName, MyShape s, boolean isDurable) {
            if (isCenter) {
                Set<Map.Entry<String, IRemoteWhiteBoard>> entrySet = connectedWhiteBoards.entrySet();
                Iterator<Map.Entry<String, IRemoteWhiteBoard>> itr = entrySet.iterator();
                while (itr.hasNext()) {
                    Map.Entry<String, IRemoteWhiteBoard> entry = itr.next();
                    String rOwnerName = entry.getKey();
                    IRemoteWhiteBoard rwb = entry.getValue();
                    if (!rOwnerName.equals(ownerName)) {
                        try {
                            if (isDurable) {
                                rwb.painted(ownerName, s, true);
                            }
                            else {
                                rwb.painting(ownerName, s, true);
                            }
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            else {
                try {
                    if (isDurable) {
                        centerWhiteBoard.painted(ownerName, s, false);
                    }
                    else {
                        centerWhiteBoard.painting(ownerName, s, false);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        private Rectangle2D.Float makeRectangle(int x1, int y1, int x2, int y2) {
            return new Rectangle2D.Float(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x1 - x2), Math.abs(y1 - y2));
        }

        private Ellipse2D.Float makeEllipse(int x1, int y1, int x2, int y2) {
            return new Ellipse2D.Float(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x1 - x2), Math.abs(y1 - y2));
        }

        private Line2D.Float makeLine(int x1, int y1, int x2, int y2){
            return new Line2D.Float(x1, y1, x2,y2);
        }

        private Ellipse2D.Float makeCircle(int x1, int y1, int x2, int y2) {
            if(Math.abs(x1 - x2)>Math.abs(y1 - y2)) {
                return new Ellipse2D.Float(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x1 - x2), Math.abs(x1 - x2));
            }else {
                return new Ellipse2D.Float(Math.min(x1, x2), Math.min(y1, y2), Math.abs(y1 - y2), Math.abs(y1 - y2));
            }
        }

        private Shape makeTextLayout() {
            Graphics2D g2d = (Graphics2D)paintSurface.getGraphics();

            FontRenderContext frc = g2d.getFontRenderContext();
            AffineTransform transform = new AffineTransform();
            transform.setToTranslation(inputPosition.getX(), inputPosition.getY()+10);
            TextLayout tl = new TextLayout(inputString, new Font("Monaco", Font.PLAIN, 18), frc);
            return tl.getOutline(transform);
        }

        public void setShapes(CopyOnWriteArrayList<MyShape> shapes) {
            this.shapes = shapes;
        }

        public void setFileName(String fileName){
            this.fileName=fileName;
        }

    }
    public void save() {
        saveJFrame.setVisible(true);
        saveJFrame.setBounds(300,300,400,400);
        JPanel savePanel=new JPanel();
        saveJFrame.add(savePanel);
        savePanel.setOpaque(false);
        savePanel.setLayout(null);
        JLabel w=new JLabel("Are you sure to save?");
        w.setBounds(100,50,150,50);
        fileNameText=new JTextField(paintSurface.fileName);
        fileNameText.setBounds(100,150,150,50);
        JButton confirm = new JButton("confirm");
        confirm.setActionCommand("confirm");
        confirm.addActionListener(new RemoteWhiteBoard.buttonListener());
        confirm.setBounds(160, 250, 70, 50);
        savePanel.add(confirm);
        savePanel.add(w);
        savePanel.add(fileNameText);
        savePanel.setVisible(true);
        saveJFrame.setVisible(true);
    }
    public void saveFile(String filePath,CopyOnWriteArrayList<MyShape> theShape){
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream
                    (new FileOutputStream(filePath));
            outputStream.writeObject(theShape);
            outputStream.close();
            saveSuccessful();
        } catch (IOException E) {
            System.out.println("Error opening the file.");
            saveFailed();
        }
    }
    public void open() {
        openJFrame =new JFrame("Open");
        openJFrame.setVisible(true);
        JFileChooser jfc = new JFileChooser();
        openJFrame.add(jfc);
        openJFrame.addWindowListener(new myWindowListener());
        String[] filterString = {".rwb"};
        MyFilter filter = new MyFilter(filterString);
        String jarFilePath = RemoteWhiteBoard.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        jfc.setCurrentDirectory(new File(jarFilePath));
        jfc.setFileFilter(filter);
        int state=jfc.showOpenDialog(null);
        if(state!=JFileChooser.APPROVE_OPTION){
            openJFrame.setVisible(false);
        }
        else{
            File fl = jfc.getSelectedFile();
            String filePath=fl.getAbsolutePath();
            String fileNn=fl.getName();
            CopyOnWriteArrayList<MyShape> theShape = new CopyOnWriteArrayList<>();
            try {
                ObjectInputStream inputStream = new ObjectInputStream
                        (new FileInputStream(filePath));
                theShape = (CopyOnWriteArrayList<MyShape>) inputStream.readObject();
                inputStream.close();
            } catch (ClassNotFoundException e) {
                System.out.println("Problems with file input1.");
            } catch (IOException e) {
                System.out.println("Problems with file input2.");
            }
            paintSurface.setShapes(theShape);
            paintSurface.repaint();
            paintSurface.synDurableShapesToAll();
            paintSurface.setFileName(fileNn.substring(0, fileNn.length() - 4));
        }
    }

    public void saveas() { ;
        saveAsJFrame.setVisible(true);
        JFileChooser jfc = new JFileChooser();
        saveAsJFrame.add(jfc);
        saveAsJFrame.addWindowListener(new myWindowListener());
        String jarFilePath = RemoteWhiteBoard.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        try {
            jarFilePath = URLDecoder.decode(jarFilePath, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        jfc.setCurrentDirectory(new File(jarFilePath));
        int state=jfc.showSaveDialog(null);
        if(state!=JFileChooser.APPROVE_OPTION){
            saveAsJFrame.setVisible(false);
        }
        else{
            File fl = jfc.getSelectedFile();
            String filepath=jfc.getCurrentDirectory()+"/"+fl.getName()+".rwb";
            CopyOnWriteArrayList<MyShape> theShape = paintSurface.shapes;
            saveFile(filepath,theShape);
        }
    }
    
    public void saveSuccessful(){
        saveJFrame.setVisible(false);
        saveAsJFrame.setVisible(false);
        saveResultJFrame =new JFrame("successful");
        saveResultJFrame.setVisible(true);
        saveResultJFrame.setBounds(300,300,350,220);
        JPanel sPanal=new JPanel();
        sPanal.setOpaque(false);
        sPanal.setLayout(null);
        saveResultJFrame.add(sPanal);
        JLabel saveSuccessful=new JLabel("Save the file Successful!");
        saveSuccessful.setBounds(80,50,200,50);
        JButton close=new JButton("close");
        close.setActionCommand("close");
        close.setBounds(159,150,80,50);
        close.addActionListener(new RemoteWhiteBoard.buttonListener());
        sPanal.add(saveSuccessful);
        sPanal.add(close);
    }
    public void saveFailed(){
        saveResultJFrame =new JFrame("faild");
        saveResultJFrame.setVisible(true);
        saveResultJFrame.setBounds(300,300,350,220);
        JPanel sPanal=new JPanel();
        sPanal.setOpaque(false);
        sPanal.setLayout(null);
        saveResultJFrame.add(sPanal);
        JLabel saveSuccessful=new JLabel("Save the file falid!");
        saveSuccessful.setBounds(80,50,200,50);
        JButton close=new JButton("close");
        close.setActionCommand("close");
        close.setBounds(159,150,80,50);
        close.addActionListener(new RemoteWhiteBoard.buttonListener());
        sPanal.add(saveSuccessful);
        sPanal.add(close);
    }
    public class buttonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if(command.equals("confirm")){
                CopyOnWriteArrayList<MyShape> theShape = paintSurface.shapes;
                paintSurface.setFileName(fileNameText.getText());
                System.out.println(paintSurface.fileName+".rwb");
                saveFile(paintSurface.fileName+".rwb",theShape);
            }
            else if(command.equals("close")){
                saveResultJFrame.setVisible(false);
            }
        }
    }
    static class myWindowListener extends WindowAdapter {
        @Override
        public void windowClosing(WindowEvent e){
            super.windowClosing(e);
        }
    }
}
