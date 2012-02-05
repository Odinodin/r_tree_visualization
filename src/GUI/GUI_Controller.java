package GUI;

import RTre.RTree;

import javax.swing.*;
import java.awt.*;

/**
 * This class holds the JFrame for the GUI.
 *
 * Author: Eirik Eide, Odin Hole Standal
 * Date: 04.okt.2004
 * Time: 13:31:05
 */
public final class GUI_Controller {
    // Constants
    public final static int TREE_PANEL_WIDTH = 865;
    public final static int TREE_PANEL_HEIGHT = 340;
    private final static int FRAME_WIDTH = 1000;
    private final static int FRAME_HEIGHT = 740;


    // GUI variables
    private final JFrame frame;
    private final Container contentPane;
    private final Main mainScreen;

    /**
     * Constructor
     * Creates a JFrame, sets its size and adds content to its Jframe. The content
     * comes from the Main-class which is a class that is bound up to the IntelliJ
     * visual GUI-builder. The Main class holds a reference to the outermost JPanel,
     * the mainPanel-variable. The mainPanel-variable is added to the JFrame's contentPane.
     *
     * @param rtre  A reference to the rtree so that the GUI can access its methods
     */
    public GUI_Controller(final RTree rtre) {
        // Setter opp GUI'en
        frame = new JFrame("R-tree by Eirik Eide and Odin Hole Standal");
        frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
        contentPane = frame.getContentPane();   // collects a reference to the contentPane.

        // Adds the mainpanel which is made with the visual editor in IntelliJ 4.5
        mainScreen = new Main(rtre);
        contentPane.add(mainScreen.mainPanel);

        // Sets the frame visible and handles program exit
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setVisible(true);
    }
}
