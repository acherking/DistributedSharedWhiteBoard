import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.font.*;

public class TimesB extends Canvas {
    private Image img;

    public TimesB() {
        setBackground(Color.white);
    }

    public void paint(Graphics g) {
        Graphics2D g2;
        g2 = (Graphics2D) g;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);

        FontRenderContext frc = g2.getFontRenderContext();
        Font f = new Font("Times",Font.BOLD, 24);
        String s = new String("24 Point Times Bold");
        TextLayout tl = new TextLayout(s, f, frc);
        Dimension theSize= getSize();
        g2.setColor(Color.green);
        tl.draw(g2, theSize.width/30, theSize.height/2);
    }

    public static void main(String s[]) {
        WindowListener l = new WindowAdapter() {
            public void windowClosing(WindowEvent e) {System.exit(0);}
            public void windowClosed(WindowEvent e) {System.exit(0);}
        };

        Frame f = new Frame("2D Text");
        f.addWindowListener(l);
        f.add("Center", new TimesB());
        f.pack();
        f.setSize(new Dimension(400, 300));
        f.show();
    }
}
