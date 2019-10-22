import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Main extends JPanel {
  public Main() {
    setBackground(Color.white);
  }

  public void paint(Graphics g) {
    Graphics2D g2D;
    g2D = (Graphics2D) g;
    FontRenderContext frc = g2D.getFontRenderContext();
    Font font1 = new Font("Courier", Font.BOLD, 24);
    String str1 = new String("Java");
    TextLayout tl = new TextLayout(str1, font1, frc);
    g2D.setColor(Color.gray);
    tl.draw(g2D, 70, 150);
  }

  public static void main(String s[]) {
    JFrame frame1 = new JFrame("2D Text");
    frame1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    frame1.getContentPane().add("Center", new Main());
    frame1.pack();
    frame1.setSize(new Dimension(500, 300));
    frame1.setVisible(true);
  }
}
