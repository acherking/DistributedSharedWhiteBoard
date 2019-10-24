import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class WhiteBoardMenu extends JMenuBar {
    private String currentShape = "Line";
    private Color currentColor = Color.BLACK;

    public WhiteBoardMenu(JMenu fileJmenu, boolean isCenter) {
        this.setToolTipText("File");
        this.setForeground(new Color(0, 0, 0));
        this.setFont(new Font("Georgia", Font.PLAIN, 12));
        this.setMargin(new Insets(0, 0, 0, 50));

        if(isCenter) {
            fileJmenu.setFont(new Font("Georgia", Font.PLAIN, 12));
            this.add(fileJmenu);
        }

        JMenu mnShape = new JMenu("Shape");
        mnShape.setFont(new Font("Georgia", Font.PLAIN, 12));
        mnShape.setHorizontalAlignment(SwingConstants.LEFT);
        this.add(mnShape);

        JMenuItem mntmLine = new JMenuItem("Line");
        mntmLine.setIcon(new ImageIcon(addPic("/icon/line.png")));
        mntmLine.setFont(new Font("Georgia", Font.PLAIN, 12));
        mnShape.add(mntmLine);
        mntmLine.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                currentShape ="Line";
            }
        });

        JMenuItem mntmCircle = new JMenuItem("Circle");
        mntmCircle.setIcon(new ImageIcon(addPic("/icon/circle.png")));
        mntmCircle.setFont(new Font("Georgia", Font.PLAIN, 12));
        mnShape.add(mntmCircle);
        mntmCircle.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                currentShape ="Circle";
            }
        });

        JMenuItem mntmRectangle = new JMenuItem("Rectangle");
        mntmRectangle.setIcon(new ImageIcon(addPic("/icon/rect.png")));
        mntmRectangle.setFont(new Font("Georgia", Font.PLAIN, 12));
        mnShape.add(mntmRectangle);
        mntmRectangle.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                currentShape ="Rectangle";
            }
        });

        JMenuItem mntmOval = new JMenuItem("Oval");
        mntmOval.setIcon(new ImageIcon(addPic("/icon/oval.png")));
        mntmOval.setFont(new Font("Georgia", Font.PLAIN, 12));
        mnShape.add(mntmOval);
        mntmOval.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                currentShape ="Oval";
            }
        });



        JMenuItem mntmCircleF = new JMenuItem("Filled Circle");
        mntmCircleF.setIcon(new ImageIcon(addPic("/icon/circlef.png")));
        mntmCircleF.setFont(new Font("Georgia", Font.PLAIN, 12));
        mnShape.add(mntmCircleF);
        mntmCircleF.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                currentShape ="CircleF";
                //System.out.print(s);
            }
        });

        JMenuItem mntmRectangleF = new JMenuItem("Filled Rectangle");
        mntmRectangleF.setIcon(new ImageIcon(addPic("/icon/rectf.png")));
        mntmRectangleF.setFont(new Font("Georgia", Font.PLAIN, 12));
        mnShape.add(mntmRectangleF);
        mntmRectangleF.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                currentShape ="RectangleF";
            }
        });

        JMenuItem mntmOvalF = new JMenuItem("Filled Oval");
        mntmOvalF.setIcon(new ImageIcon(addPic("/icon/ovalf.png")));
        mntmOvalF.setFont(new Font("Georgia", Font.PLAIN, 12));
        mnShape.add(mntmOvalF);
        mntmOvalF.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                currentShape ="OvalF";
            }
        });
        JMenu mnStyle = new JMenu("Color");
        mnStyle.setFont(new Font("Georgia", Font.PLAIN, 12));
        this.add(mnStyle);

        JMenuItem mntmRed = new JMenuItem();
        mntmRed.setBackground(Color.RED);
        mnStyle.add(mntmRed);
        mntmRed.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                currentColor =Color.RED;
            }
        });

        JMenuItem mntmOrange = new JMenuItem();
        mntmOrange.setBackground(Color.ORANGE);
        mnStyle.add(mntmOrange);
        mntmOrange.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                currentColor =Color.ORANGE;
            }
        });

        JMenuItem mntmYellow = new JMenuItem();
        mntmYellow.setBackground(Color.YELLOW);
        mnStyle.add(mntmYellow);
        mntmYellow.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                currentColor =Color.YELLOW;
            }
        });

        JMenuItem mntmGreen = new JMenuItem();
        mntmGreen.setBackground(Color.GREEN);
        mnStyle.add(mntmGreen);
        mntmGreen.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                currentColor =Color.GREEN;
            }
        });

        JMenuItem mntmCyan = new JMenuItem();
        mntmCyan.setBackground(Color.CYAN);
        mnStyle.add(mntmCyan);
        mntmCyan.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                currentColor =Color.CYAN;
            }
        });

        JMenuItem mntmBlue = new JMenuItem();
        mntmBlue.setBackground(Color.BLUE);
        mnStyle.add(mntmBlue);
        mntmBlue.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                currentColor =Color.BLUE;
            }
        });

        JMenuItem mntmMagenta = new JMenuItem();
        mntmMagenta.setBackground(Color.MAGENTA);
        mnStyle.add(mntmMagenta);
        mntmMagenta.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                currentColor =Color.MAGENTA;
            }
        });

        JMenuItem mntmPink = new JMenuItem();
        mntmPink.setBackground(Color.PINK);
        mnStyle.add(mntmPink);
        mntmPink.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                currentColor =Color.PINK;
            }
        });

        JMenuItem mntmBlack = new JMenuItem();
        mntmBlack.setBackground(Color.BLACK);
        mnStyle.add(mntmBlack);
        mntmBlack.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                currentColor =Color.BLACK;
            }
        });

        JMenuItem mntmWhite = new JMenuItem();
        mntmWhite.setBackground(Color.WHITE);
        mnStyle.add(mntmWhite);
        mntmWhite.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                currentColor =Color.WHITE;
            }
        });

        JMenuItem mntmGray = new JMenuItem();
        mntmGray.setBackground(Color.GRAY);
        mnStyle.add(mntmGray);
        mntmGray.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                currentColor =Color.GRAY;
            }
        });

        JMenuItem mntmOlive = new JMenuItem();
        mntmOlive.setBackground(new Color(128, 128, 0));
        mnStyle.add(mntmOlive);
        mntmOlive.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                currentColor =new Color(128, 128, 0);
            }
        });

        JMenuItem mntmPeru = new JMenuItem();
        mntmPeru.setBackground(new Color(205, 133, 63));
        mnStyle.add(mntmPeru);
        mntmPeru.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                currentColor =new Color(205, 133, 63);
            }
        });

        JMenuItem mntmTeal = new JMenuItem();
        mntmTeal.setBackground(new Color(0, 128, 128));
        mnStyle.add(mntmTeal);
        mntmTeal.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                currentColor =new Color(0, 128, 128);
            }
        });

        JMenuItem mntmTomato = new JMenuItem();
        mntmTomato.setBackground(new Color(255, 99, 71));
        mnStyle.add(mntmTomato);
        mntmTomato.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                currentColor =new Color(255, 99, 71);
            }
        });

        JMenuItem mntmBisque = new JMenuItem();
        mntmBisque.setBackground(new Color(255, 228, 196));
        mnStyle.add(mntmBisque);
        mntmBisque.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                currentColor =new Color(255, 228, 196);
            }
        });

        JMenu mnTool = new JMenu("Tool");
        mnTool.setFont(new Font("Georgia", Font.PLAIN, 12));
        this.add(mnTool);

        JMenuItem mntmEraserS = new JMenuItem("Small Eraser");
        mntmEraserS.setIcon(new ImageIcon(addPic("/icon/eraser.png")));
        mntmEraserS.setFont(new Font("Georgia", Font.PLAIN, 12));
        mnTool.add(mntmEraserS);
        mntmEraserS.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                currentShape ="EraserS";
            }
        });

        JMenuItem mntmEraserM = new JMenuItem("Medium Eraser");
        mntmEraserM.setIcon(new ImageIcon(addPic("/icon/eraser.png")));
        mntmEraserM.setFont(new Font("Georgia", Font.PLAIN, 12));
        mnTool.add(mntmEraserM);
        mntmEraserM.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                currentShape ="EraserM";
            }
        });


        JMenuItem mntmEraserL = new JMenuItem("Large Eraser");
        mntmEraserL.setIcon(new ImageIcon(addPic("/icon/eraser.png")));
        mntmEraserL.setFont(new Font("Georgia", Font.PLAIN, 12));
        mnTool.add(mntmEraserL);
        mntmEraserL.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                currentShape ="EraserL";
            }
        });

        JMenuItem mntmBrush = new JMenuItem("Painting Brush");
        mntmBrush.setIcon(new ImageIcon(addPic("/icon/brush.png")));
        mntmBrush.setFont(new Font("Georgia", Font.PLAIN, 12));
        mnTool.add(mntmBrush);
        mntmBrush.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                currentShape ="Painting Brush";
            }
        });


        JMenuItem mntmPen = new JMenuItem("Draw Pen");
        mntmPen.setIcon(new ImageIcon(addPic("/icon/pen.png")));
        mntmPen.setFont(new Font("Georgia", Font.PLAIN, 12));
        mnTool.add(mntmPen);
        mntmPen.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                currentShape ="Draw Pen";
            }
        });

        JMenu mnInsert = new JMenu("Insert");
        mnInsert.setFont(new Font("Georgia", Font.PLAIN, 12));
        this.add(mnInsert);

        JMenuItem mntmText = new JMenuItem("Text");
        mntmText.setIcon(new ImageIcon(addPic("/icon/text.png")));
        mntmText.setFont(new Font("Georgia", Font.PLAIN, 12));
        mnInsert.add(mntmText);
        mntmText.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                currentShape ="Text";
            }
        });

        JMenuItem mntmPic = new JMenuItem("Picture");
        mntmPic.setIcon(new ImageIcon(addPic("/icon/pic.png")));
        mntmPic.setFont(new Font("Georgia", Font.PLAIN, 12));
        mnInsert.add(mntmPic);
        mntmPic.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                currentShape ="Picture";
            }
        });
    }

    public String getCurrentShape() {
        return currentShape;
    }

    public Color getCurrentColor() {
        return currentColor;
    }

    public void setCurrentColor(Color currentColor) {
        this.currentColor = currentColor;
    }

    public void setCurrentShape(String currentShape) {
        this.currentShape = currentShape;
    }

    public Image addPic(String path) {//Get image from the given path.
        Image img=null;
        URL url = RemoteWhiteBoard.class.getResource(path);
        try {
            InputStream input=url.openStream();
            img= ImageIO.read(input);
        }catch(IOException e) {
            System.out.print("Problem with get the image through path: "+path+"\n");
        }
        return img;
    }
}
