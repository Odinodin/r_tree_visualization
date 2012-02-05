package GUI;

import RTre.*;
import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;
import java.util.ArrayList;

/**
 * This class extends JPanel and overrides the update(Graphics g)
 * (which is called by the paint(..) method) so that we can custom-paint the RTree as
 * a tree.
 *
 * Author: Eirik Eide, Odin Hole Standal
 * Date: 04.okt.2004
 * Time: 14:41:45
 */
final class TreeViewer extends JPanel  {

    // Tree-position constants
    private final int xSpacing = 4;         // The space between databoxes.
    private final int boxDimension = 6;     // All boxes are quadratic
    private final int ySpacing = 25;        // The space between two leafnodes
    private final int extraSpacing = 20;    // Extra spacing to make som air around the tree.

    private Dimension size;

    private Graphics2D g2d;
    private final RTree rtree;
    private boolean showHelp;

    /**
     * Constructor
     * Initialises variables
     *
     * @param rtree     Reference to the Rtree so that can be painted
     */
    public TreeViewer(final RTree rtree) {
        this.setOpaque(false);  // Has to be false to paint correctly
        this.setPreferredSize(new Dimension(GUI_Controller.TREE_PANEL_WIDTH,
                                            GUI_Controller.TREE_PANEL_HEIGHT));
        this.rtree = rtree;
        showHelp = true;
    }

    /**
     * Sends the Graphics object to the update() method in this class.
     *
     * @param g     Graphics2D object that can paint to this JPanel
     */
    public void paint(final Graphics g){
        update(g);
    }

    /**
     * This is where the painting of the tree-structure happens
     *
     * @param g     Graphics2D object that can paint to this JPanel
     */
    public void update(final Graphics g){
        g2d = (Graphics2D)g;
        // Sets the stroke thickness
        g2d.setStroke(new BasicStroke((float) 1.5));
        // Enables antialiasing for better image quality
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background-filling
        g2d.setColor(new Color(250,250,250));

        if (showHelp){
            g2d.fillRect(0,0, GUI_Controller.TREE_PANEL_WIDTH+50, GUI_Controller.TREE_PANEL_HEIGHT+50);
            drawHelpScreen(g2d);

        // Draw the tree
        } else if(size!=null){
            int height = (int)size.getHeight();
            int width = (int)size.getWidth();

            if (height < GUI_Controller.TREE_PANEL_HEIGHT+50){
                height = GUI_Controller.TREE_PANEL_HEIGHT+50;
            }
            if (width < GUI_Controller.TREE_PANEL_WIDTH+50){
                width = GUI_Controller.TREE_PANEL_WIDTH+50;
            }
            g2d.fillRect(0,0, width, height);
            drawTree(g2d, extraSpacing, extraSpacing,
                    (int)(size.getWidth()-extraSpacing),
                    ySpacing+boxDimension, rtree.getRootNode());
        } else{
            g2d.fillRect(0,0, GUI_Controller.TREE_PANEL_WIDTH+50, GUI_Controller.TREE_PANEL_HEIGHT+50);
        }
    }

     /**
     * This method calls itself recursively to traverse down the tree. The painting
     * starts with the root node.
     *
     * @param g2d       The Graphics2D object used to paint
     * @param x         The xPos of the start of the Area that the TreeElement can paint
     * @param y         The yPos of the start of the Area that the TreeElemtent can paint
     * @param width     The width of the Area that the TreeElement is allowed to paint in
     * @param height    The heigh of the Area that the TreeElement is allowd to paint in
     * @param root      The TreeElement root of the current level.
     */
    private void drawTree(final Graphics2D g2d, int x, int y, final int width, final int height, final TreeElement root){
        //finn posisjoner
        if (root instanceof DataObject){
            final int startAtx;
            final int center = width/2;
            startAtx = x + center-boxDimension/2;
            ((DataObject) root).drawInTree(g2d,startAtx,y,boxDimension);
        } else{
            final Node node = (Node) root;
            //Tegn Node
            node.drawInTree(g2d,x,y,width,height,boxDimension);

            //Rekursivt kall for hvert barn
            TreeElement child;
            final int childWidth = width/rtree.maxChildrenPerNode;
            y+= height;
            for (int i = 0; i < node.getSize(); i++){
                //Beregn posisjon
                child = (TreeElement) node.children.get(i);
                //Kall
                drawTree(g2d,x,y,childWidth, height, child);
                x+=childWidth;
            }
        }
    }

     /**
     * Adjust the size of the current canvas so that the ScrollPane that holds this
     * Panel so that the scrollbars have the correct size.
     */
    public void adjust() {
        size = calculateCanvasSize();
        this.setPreferredSize(size);    // Sets the scrollPane size

        repaint();
        // Updates the Jpanel and the scrollpane
        revalidate();
    }

    /**
     * Calculates the canvas size based on the current Rtree.
     *
     *
     * @return     a Dimension instance that hold the new canvas size
     */
    private Dimension calculateCanvasSize() {
        final int numberOfLevels = rtree.getNumberOfLevels();
        final int M = rtree.maxChildrenPerNode;
        final int width;
        final int height;

        width = xSpacing + (boxDimension + xSpacing )*  (int)(Math.pow(M, (numberOfLevels-1))) + extraSpacing;
        height = numberOfLevels * (boxDimension + ySpacing) + extraSpacing;
        return new Dimension(width, height);
    }

    /**
     * Toggles the help text
     */
    public void toggleHelp(){
        if (!showHelp){
            showHelp = true;
        }else{
            showHelp = false;
        }
        repaint();
    }

    /**
     * Hides the help text
     */
    public void hideHelp(){
        showHelp = false;
    }


    /**
     * Draws the help screen
     * @param g2d   The drawing object
     */
    private void drawHelpScreen(final Graphics2D g2d) {
        final String[] helptext = {
            "Add",
            "   Press the add button to add polygons to the r tree, set the number of polygons to add in the adjacent combobox.",
            "   Use the polygon size combo boxes to set the maximum polygon widht and height. Adding a large number of poylgons",
            "   may cause slow selection.",
            "",
            "R-Tree parameters",
            "   Set M and/or m to adjust the R-tree. Altering the values after a tree is built will cause the tree to be rebuilt.",
            "",
            "Selection",
            "   Click and drag anywhere in the bottommost screen to select any number of polygons.",
            "   The selected polygons will be highlighted.",
            "",
            "Delete",
            "   To delete a selection, press the delete button.",
            "",
            "Clear",
            "   Press the clear button to start over with an empty tree.",
            "",
            "Help",
            "   Pressing the help button toggles this message on/off."

        };

        g2d.setColor(Color.BLACK);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);
        for (int i = 0; i < helptext.length;i++){
            g2d.drawString(helptext[i],20,15*(i+1));
        }
    }
}


