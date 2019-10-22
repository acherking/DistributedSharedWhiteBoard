import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.font.*;

public class TextClipping extends Canvas {

    public TextClipping() {
        setBackground(Color.white);
    }

    public void paint(Graphics g) {
        Graphics2D g2;
        g2 = (Graphics2D) g;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);

        int w = getSize().width;
        int h = getSize().height;

        FontRenderContext frc = g2.getFontRenderContext();
        Font f = new Font("Times New Roman Bold",Font.PLAIN,w/8);
        String s = new String("Vincent van Gogh");
        TextLayout tl = new TextLayout(s, f, frc);
        float sw = (float) tl.getBounds().getWidth();
        AffineTransform transform = new AffineTransform();
        transform.setToTranslation(w/2-sw/2,h/6);
        Shape shape = tl.getOutline(transform);
        g2.setClip(shape);
        g2.setColor(Color.blue);
        g2.fill(shape.getBounds());
        g2.setColor(Color.white);
        f = new Font("Helvetica",Font.BOLD,10);
        tl = new TextLayout("+", f, frc);
        sw = (float) tl.getBounds().getWidth();

        Rectangle r = shape.getBounds();
        int x = r.x;
        int y = r.y;
        while ( y < (r.y + r.height+(int) tl.getAscent()) ) {
            tl.draw(g2, x, y);
            if ((x += (int) sw) > (r.x+r.width)) {
                x = r.x;
                y += (int) tl.getAscent();
            }
        }

    }

    public static void main(String s[]) {
        WindowListener l = new WindowAdapter() {
            public void windowClosing(WindowEvent e) {System.exit(0);}
            public void windowClosed(WindowEvent e) {System.exit(0);}
        };

        Frame f = new Frame("2D Text");
        f.addWindowListener(l);
        f.add("Center", new TextClipping());
        f.pack();
        f.setSize(new Dimension(400, 300));
        f.show();
    }
}