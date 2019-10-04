import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.util.ArrayList;

import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class DrawingBoardWithMatrix extends JFrame {
  ToolBar toolBar;

  public static void main(String[] args) {
    new DrawingBoardWithMatrix();
  }

  public DrawingBoardWithMatrix() {
    this.setSize(300, 300);
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.add(new PaintSurface(), BorderLayout.CENTER);

    toolBar = new ToolBar();
    this.add(toolBar, BorderLayout.WEST);
    this.setVisible(true);
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
      else {
        System.out.printf("In ToolBar.actionPerformed: unknow command [%s]\n", command);
      }
    }

    public String getCurrentState() {
      return currentState;
    }
  }

  private class PaintSurface extends JComponent {
    ArrayList<Shape> shapes = new ArrayList<Shape>();

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
          //Shape r = makeRectangle(startDrag.x, startDrag.y, e.getX(), e.getY());
          //Shape r = makeEllipse(startDrag.x, startDrag.y, endDrag.x, endDrag.y);
          Shape r = createNewShape();
          if (r!=null) shapes.add(r);
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

      if (startDrag != null && endDrag != null && path != null) {
        g2.setPaint(Color.LIGHT_GRAY);
        //Shape r = makeRectangle(startDrag.x, startDrag.y, endDrag.x, endDrag.y);
        //Shape r = makeEllipse(startDrag.x, startDrag.y, endDrag.x, endDrag.y);
        Shape r = createNewShape();
        if(r!=null) g2.draw(r);
      }
    }

    private Shape createNewShape() {
      String cs = toolBar.getCurrentState();
      Shape r = null;

      if(cs.equals("Rect")) {
        r = makeRectangle(startDrag.x, startDrag.y, endDrag.x, endDrag.y);;
      }
      else if(cs.equals("Oval")) {
        r = makeEllipse(startDrag.x, startDrag.y, endDrag.x, endDrag.y);;
      }
      else if(cs.equals("Path")) {
        r = (Shape) path.clone();
      }
      else {
        System.out.printf("In PaintSurface.createNewShape: unknow currentState [%s]\n", cs);
      }

      return r;
    }

    private Rectangle2D.Float makeRectangle(int x1, int y1, int x2, int y2) {
      return new Rectangle2D.Float(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x1 - x2), Math.abs(y1 - y2));
    }

    private Ellipse2D.Float makeEllipse(int x1, int y1, int x2, int y2) {
      return new Ellipse2D.Float(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x1 - x2), Math.abs(y1 - y2));
    }
  }
}