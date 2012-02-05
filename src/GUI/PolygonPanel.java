package GUI;

import RTre.*;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Calendar;

import GUI.Listeners.SelectionListener;

/**
 * This class extends JPanel and overrides the update(Graphics g)
 * (which is called by the paint(..) method) so that we can custom-paint the RTree as
 * overlapping bounding boxes.
 *
 * Author: Eirik Eide, Odin Hole Standal
 * Date: 04.okt.2004
 * Time: 14:41:45
 */
public final class PolygonPanel extends JPanel  {

    private Graphics2D g2d;        // Used to draw to this JPanel
    private final RTree rtree;     // Reference to the RTree instance
    private double selectionTime;  // Time which is used to constrain the repainting.

    private final Rectangle selectionArea;
    private final Rectangle previousSelectionArea;
    public boolean mousePressed;
    private final Main main;

     /**
     * Constructor
     * Adds listeners and initialises variables
     *
     * @param rtree     Reference to the rtree so that we can access its rootNode and methods
     * @param main      Reference to the Main-class instance which holds the GUI-elements.
     */
    public PolygonPanel(final RTree rtree, final Main main) {
        this.rtree = rtree;
        this.main = main;
        // Adds an actionListener to this panel
        final SelectionListener sListener= new SelectionListener(this);
        this.addMouseListener(sListener);
        this.addMouseMotionListener(sListener);


        // The mouse is not pressed initially
        mousePressed = false;

        // Has to be false to paint correctly
        this.setOpaque(false);

        selectionArea = new Rectangle();
        previousSelectionArea = new Rectangle();
        this.setPreferredSize(new Dimension(1000, 1000));
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
     * This is where the painting of the Polygons and boundingboxes happens.
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
        g2d.fillRect(0,0, 1000, 1000);

        // Draw the tree
        drawTree(g2d);

        if (mousePressed){
            drawSelection(g2d);
        }
    }

       /**
     * This method is called when the user starts a selection in the polygonwindow.
     * Important variables are updated and the current selection is removed.
     */
    public void selectionStarted() {
        // Update the mousePressed boolean
        mousePressed = true;

        // Remove the current selection
        rtree.deselect();
        rtree.releaseMostRecent();
    }

     /**
     * Called when the mousePressed boolean is true. Draws the selection according to
     * the startingposistion and endposition of the current selection.
     * These position values are stored in the selectionArea-object
     *
     * @param g2d   The graphics2d object
     */
    private void drawSelection(final Graphics2D g2d) {
        g2d.setColor(new Color(255, 0, 0, 50));

        g2d.fill(selectionArea);
        g2d.setColor(new Color(255,0,0, 240));
        g2d.draw(selectionArea);
    }

    /**
     * Draws the Rtree by breadth.first traversation from the root-node. The painting
     * happens by calls to draw-methods in the classes DataObject and Node
     * as the tree is traversed.
     *
     * @param g2d  the Graphics2D object which can paint to this JPanel
     */
    private void drawTree(final Graphics2D g2d){
        final Node root = rtree.getRootNode();

        final ArrayList drawQueue = new ArrayList();
        drawQueue.add(root);
        TreeElement bBox;

        while(!drawQueue.isEmpty()){
            bBox = (TreeElement)drawQueue.remove(0);
            bBox.drawMe(g2d);
            if (!(bBox instanceof DataObject)){
                drawQueue.addAll(((Node)bBox).getChildren());
            }
        }
    }


     /**
     * Calculates and updates the selectionRectangle (of the Rectangle-class) variable
     * based on the start and end positions of the current selection.
     *
     * @param startX    The start of the selection (X-pos)
     * @param startY    The start of the selection (Y-pos)
     * @param x         The end of the selection (X-pos)
     * @param y         The end of the selection (Y-pos)
     */
    public void calculateSelectionRectangle(final int startX, final int startY, final int x, final int y) {

        previousSelectionArea.setLocation((int)selectionArea.getX()-2,(int)selectionArea.getY()-2);
        previousSelectionArea.setSize((int)selectionArea.getWidth()+4,(int)selectionArea.getHeight()+4);

        final int rectangleX;
        final int rectangleY;
        final int rectangleWidth;
        final int rectangleHeigth;

        if(startX < x){
            rectangleX = startX;
            rectangleWidth = x - startX;
        } else{
            rectangleX = x;
            rectangleWidth = startX - x;
        }

        if(startY < y){
            rectangleY = startY;
            rectangleHeigth = y - startY;
        } else{
            rectangleY = y;
            rectangleHeigth = startY - y;
        }
        selectionArea.setLocation(rectangleX,rectangleY);
        selectionArea.setSize(rectangleWidth,rectangleHeigth);
        repaint(previousSelectionArea);
        repaint(selectionArea);

    }

     /**
     * Called when the user releases his mousebutton after having done a selection.
     * The findLeaves method in the Rtree is invoked which tags all the nodes in the
     * selection area as selected. All nodes that are tagged as selected are then
     * painted in a distinct color in the subsequent call to repaint().
     */
    public void selectNodes(){
        repaint();
        if (rtree.getRootNode().getSize() != 0){
            rtree.findLeaves(selectionArea);
            main.updateGUI();
            repaint();
        }

    }

}


