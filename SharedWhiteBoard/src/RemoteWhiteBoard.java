import remote.IRemoteWhiteBoard;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class RemoteWhiteBoard extends UnicastRemoteObject implements IRemoteWhiteBoard {
    private JFrame baseJFrame;
    private ToolBar toolBar;
    private PaintSurface paintSurface;
    private String userName;
    private String rwbName;
    private boolean isCenter;
    private IRemoteWhiteBoard centerWhiteBoard;
    private ConcurrentHashMap<String, IRemoteWhiteBoard> rWhiteBoards;

    public RemoteWhiteBoard() throws RemoteException {
        this.userName = "initUserName";
        this.rwbName = "initRWBName";
        this.isCenter = false;
        this.centerWhiteBoard = null;
        this.rWhiteBoards = null;

        baseJFrame = new JFrame();
        baseJFrame.setSize(300, 300);
        baseJFrame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent){
                exitWhiteBoard();
            }
        });
        paintSurface = new PaintSurface();
        baseJFrame.add(paintSurface, BorderLayout.CENTER);

        toolBar = new ToolBar();
        baseJFrame.add(toolBar, BorderLayout.WEST);
    }

    public RemoteWhiteBoard(String userName, String rwbName, boolean isCenter, IRemoteWhiteBoard centerRWB) throws RemoteException {
        this();

        this.userName = userName;
        this.rwbName = rwbName;
        this.isCenter = isCenter;
        baseJFrame.setTitle(userName+"-"+rwbName);

        if (this.isCenter)  {
            rWhiteBoards = new ConcurrentHashMap<>();
        }
        else {
            this.centerWhiteBoard = centerRWB;
        }
    }

    public void showWhiteBoard() {
        baseJFrame.setVisible(true);
    }

    public void exitWhiteBoard() {
        if (isCenter) {
            exitAllRWB();
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
        Set<Map.Entry<String, IRemoteWhiteBoard>> entrySet = rWhiteBoards.entrySet();
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
    }

    @Override
    public void painting(String name, Shape temperShape, boolean fromCenter) throws RemoteException {
        paintSurface.addTemperShape(name, temperShape, fromCenter);
        paintSurface.repaint();
    }

    @Override
    public void painted(String name, Shape durableShape, boolean fromCenter) throws RemoteException {
        paintSurface.addDurableShape(name, durableShape, fromCenter);
        paintSurface.repaint();
    }

    @Override
    public boolean enterRequest(String applierName, String remoteHostName) throws RemoteException {
        Registry remoteRegistry = LocateRegistry.getRegistry(remoteHostName);
        try {
            IRemoteWhiteBoard rwb = (IRemoteWhiteBoard) remoteRegistry.lookup(applierName+"-"+rwbName);
            addRemoteWhiteBoard(applierName, rwb);
            return true;
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void dropoutRequest(String applierName) throws RemoteException {
        removeRemoteWhiteBoard(applierName);
    }

    @Override
    public void exitCurrentWhiteBoard() throws RemoteException {
        baseJFrame.setVisible(false);
        baseJFrame.dispose();
    }

    // for center
    public void addRemoteWhiteBoard(String name, IRemoteWhiteBoard rwb) {
        rWhiteBoards.put(name, rwb);
    }

    public void removeRemoteWhiteBoard(String name) {
        rWhiteBoards.remove(name);
    }

    private class ToolBar extends JPanel implements ActionListener {
        String currentState = "Rect";

        public ToolBar() {
            this.setLayout(new FlowLayout());

            JRadioButton rectButton = new JRadioButton("Rect");
            rectButton.setActionCommand("Rect");
            rectButton.addActionListener(this);

            JRadioButton ovalButton = new JRadioButton("Oval");
            ovalButton.setActionCommand("Oval");
            ovalButton.addActionListener(this);

            JRadioButton pathButton = new JRadioButton("Path");
            pathButton.setActionCommand("Path");
            pathButton.addActionListener(this);

            ButtonGroup shapeOption = new ButtonGroup();
            shapeOption.add(rectButton);
            shapeOption.add(ovalButton);
            shapeOption.add(pathButton);

            this.add(rectButton);
            this.add(ovalButton);
            this.add(pathButton);
        }

        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();

            if(command.equals("Rect")) {
                currentState = "Rect";
            }
            else if(command.equals("Oval")) {
                currentState = "Oval";
            }
            else if(command.equals("Path")) {
                currentState = "Path";
            }
            else if(command.equals("Save as")) {
                // paintSurface.saveas();
            }
            else {
                System.out.printf("In ToolBar.actionPerformed: unknown command [%s]\n", command);
            }
        }

        public String getCurrentState() {
            return currentState;
        }
    }

    protected class PaintSurface extends JComponent {
        private CopyOnWriteArrayList<Shape> shapes = new CopyOnWriteArrayList<>();
        private ConcurrentHashMap<String, Shape> temperShapes = new ConcurrentHashMap<>();
        Point startDrag, endDrag;
        Path2D path;

        public PaintSurface() {
            this.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    startDrag = new Point(e.getX(), e.getY());
                    endDrag = startDrag;
                    path = new Path2D.Float();
                    path.moveTo(e.getX(), e.getY());
                    repaint();
                }

                public void mouseReleased(MouseEvent e) {
                    addDurableShape(userName, createNewShape(), false);
                    startDrag = null;
                    endDrag = null;
                    path = null;
                    repaint();
                }
            });

            this.addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseDragged(MouseEvent e) {
                    endDrag = new Point(e.getX(), e.getY());
                    path.lineTo(e.getX(), e.getY());
                    addTemperShape(userName, createNewShape(), false);
                    repaint();
                }
            });
        }
        private void paintBackground(Graphics2D g2){
            g2.setPaint(Color.LIGHT_GRAY);
            for (int i = 0; i < getSize().width; i += 10) {
                Shape line = new Line2D.Float(i, 0, i, getSize().height);
                g2.draw(line);
            }

            for (int i = 0; i < getSize().height; i += 10) {
                Shape line = new Line2D.Float(0, i, getSize().width, i);
                g2.draw(line);
            }


        }
        public void paint(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            paintBackground(g2);
            Color[] colors = { Color.YELLOW, Color.MAGENTA, Color.CYAN , Color.RED, Color.BLUE, Color.PINK};
            int colorIndex = 0;

            g2.setStroke(new BasicStroke(2));
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.50f));

            for (Shape s : shapes) {
                g2.setPaint(Color.BLACK);
                g2.draw(s);
                if(!(s instanceof Path2D.Float)) {
                    g2.setPaint(colors[(colorIndex++) % 6]);
                    g2.fill(s);
                }
            }

            Set<Map.Entry<String, Shape>> entrySet = temperShapes.entrySet();
            Iterator<Map.Entry<String, Shape>> itr = entrySet.iterator();
            while (itr.hasNext()) {
                Map.Entry<String, Shape> entry = itr.next();
                String ownerName = entry.getKey();
                Shape shape = entry.getValue();
                g2.setPaint(Color.BLACK);
                g2.draw(shape);
            }

        }

        private Shape createNewShape() {
            String cs = toolBar.getCurrentState();
            Shape r = null;

            switch (cs) {
                case "Rect":
                    r = makeRectangle(startDrag.x, startDrag.y, endDrag.x, endDrag.y);
                    break;
                case "Oval":
                    r = makeEllipse(startDrag.x, startDrag.y, endDrag.x, endDrag.y);
                    break;
                case "Path":
                    r = (Shape) path.clone();
                    break;
                default:
                    System.out.printf("In PaintSurface.createNewShape: unknown currentState [%s]\n", cs);
                    break;
            }
            return r;
        }

        public void addTemperShape(String ownerName, Shape s, boolean fromCenter) {
            temperShapes.put(ownerName, s);
            if (!fromCenter) {
                addShapeToRWB(ownerName, s, false);
            }
        }

        public void addDurableShape(String ownerName, Shape s, boolean fromCenter) {
            temperShapes.remove(ownerName);
            shapes.add(s);
            if (!fromCenter) {
                addShapeToRWB(ownerName, s, true);
            }
        }

        public void addShapeToRWB(String ownerName, Shape s, boolean isDurable) {
            if (isCenter) {
                Set<Map.Entry<String, IRemoteWhiteBoard>> entrySet = rWhiteBoards.entrySet();
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
    }
}
