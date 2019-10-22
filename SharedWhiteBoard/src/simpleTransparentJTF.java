/**
 * File: simpleTransparentJTF.java
 * Tiltle: Transparent JTextField | The Simple and Basic way to do it.
 * Author: http://java-code-complete.blogspot.com/
 */

//Java Core Package
import javax.swing.*;
//Java Extension Package
import java.awt.*;

public class simpleTransparentJTF extends JFrame {

    //Constructing JTextField
    JTextField field = new JTextField("Type your Text Here...",20);

    //Initiazlizing the class Font to set our JTextField text style
    Font defualtFont;

    //Setting up GUI
    public simpleTransparentJTF() {

        //Setting up the Title of the Window
        super("Transparent JTextField");

        //Set Size of the Window (WIDTH, HEIGHT)
        setSize(370,85);

        //Exit Property of the Window
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Constructing class Font property
        defualtFont = new Font("Arial", Font.BOLD, 18);

        //Setting JTextField Properties
        field.setFont(defualtFont);
        field.setPreferredSize(new Dimension(150,40));
        field.setForeground(Color.BLACK);

        //Step 1: Remove the border line to make it look like a flat surface.
        field.setBorder(BorderFactory.createLineBorder(Color.white, 0));

        //Step 2: Set the background color to null to remove the background.
        field.setBackground(null);

        //Setting up the container ready for the components to be added.
        Container pane = getContentPane();
        setContentPane(pane);

        //Change the background color of the container to see
        //if the JTextField is really transparent.
        pane.setBackground(Color.YELLOW);

        //Adding JTextField to our container
        pane.add(field);

        //Setting up the container layout
        FlowLayout flow = new FlowLayout(FlowLayout.CENTER);
        pane.setLayout(flow);

        /**Set all the Components Visible.
         * If it is set to "false", the components in the container will not be visible.
         */
        setVisible(true);

        //Disable Frame Size
        //setResizable(false);
    }

    //Main Method
    public static void main (String[] args) {
        simpleTransparentJTF pjtf = new simpleTransparentJTF();
    }
}