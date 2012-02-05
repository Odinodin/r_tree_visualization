package RTre;

import GUI.GUI_Controller;

import java.awt.*;
import java.util.ArrayList;

/**
 * DataObjects are represented as polygons with 3 to 9 points based on a random number.
 * They are the lowest nodes in the tree.
 * This class has methods for painting the DataObjects in both the TreeView and the
 * PolygonView.
 *
 * Author: Eirik Eide, Odin Hole Standal
 * Date: 04.okt.2004
 * Time: 13:28:44
 */
public final class DataObject implements TreeElement{

    private final Polygon polygon;
    private Node parent;
    private Color fillcolor;
    private Color strokecolor;
    public boolean isSelected;
    public boolean isMostRecent;
    private final int level;
    private final RTree rtree;


     /**
     * Constructor
     * Initializes the values of this instance.
     *
     * @param rtree     Reference to the RTree in which this node is placed
     */
    public DataObject(final RTree rtree) {
        this.rtree = rtree;
        level = 0;
        isSelected = false;
        polygon = createNewPolygon();
    }

    /**
     * Returns the parent of this DataObject
     *
     * @return   the parent of this DataObject
     */
    public Node getParent() {
        return parent;
    }

    /**
     * Sets the new parent of this DataObject
     *
     * @param parent    the new Parent of this DataObject
     */
    public void setParent(final Node parent){
        this.parent = parent;
    }


     /**
     * Returns the bounding box of this dataObject.
     * It is calculated through the polygon-variable.
     *
     * @return  the boundingbox of this dataObject
     */
    public Rectangle getBoundingBox(){
        return polygon.getBounds();
    }

     /**
     * Returns the level of this DataObject.
     * All DataObject are level 0.
     *
     * @return  the level of this DataObject
     */
    public int getLevel() {
        return level;
    }


    /**
     * Returns true if this DataObjects polygon intersects with the rectangle parameter.
     *
     * @param r     The rectangle to calculate intersection with
     * @return      true if the polygon intersects.
     */
    public boolean intersects(final Rectangle r){
        return polygon.intersects(r);
    }


    /**
     * Gives this dataObject a color based on the fillcolor of its Parent.
     * By getting the color this way, we can make all the nodes that share a parent
     * have the same color.
     */
    public void setColor() {
        fillcolor = parent.fillcolor;
        final int red = fillcolor.getRed() + 10;
        final int green = fillcolor.getGreen() + 10;
        final int blue = fillcolor.getBlue() + 10;
        if(isSelected || isMostRecent){
            fillcolor =  new Color(0,0,0, 160);
            strokecolor = new Color(0,0,0, 200);
        } else{
            strokecolor = new Color(red,green,blue, 200);
        }
    }



    /**
     * Creates a new Polygon instance and gives it random points and translates it to a
     * random place on the JPanel in such a way that it is always inside the bounds of the
     * JPanel.
     *
     * We assume that all polygons have at lease 3 points.
     *
     * @return  a new polygon
     */
    private Polygon createNewPolygon() {
        final int antallPunkt = (int) (Math.random()*7) + 3;  // Bestemmer antall punkt til å være i området [3,9]

        final int[] xList = new int[antallPunkt];
        final int[] yList = new int[antallPunkt];

        // Defines the pointlitst to be inside Max/Min in the x and y direction.
        // At the same time the polygon is translated to a random coordinate, but
        // it will still be inside the boundaries of the screen.

        final int xOffset;
        final int yOffset;
        xOffset = (int)(Math.random()* (GUI_Controller.TREE_PANEL_WIDTH - rtree.dataObjectWidth));
        yOffset = (int)(Math.random()* (GUI_Controller.TREE_PANEL_HEIGHT - rtree.dataObjectHeight) );

        for (int i=0; i<antallPunkt; i++){
            xList[i] = (int)(Math.random() * rtree.dataObjectWidth) + xOffset;
            yList[i] = (int)(Math.random() * rtree.dataObjectHeight) + yOffset;
        }

        // Returns the finished polygon
        return new Polygon(xList, yList, antallPunkt);
    }


     /**
     * Draws this DataObject in the PolygonView (the bottomost panel)
     *
     * @param g2d       The graphics2D object that paints to the PolygonView JPanel
     */
    public void drawMe(final Graphics2D g2d) {
        g2d.setColor(fillcolor);
        g2d.fill(polygon);
        g2d.setColor(strokecolor);
        g2d.draw(polygon);

    }


    /**
     * Draws the DataObject in the TreeView at the parameter positions
     *
     * @param g2d           The Graphics2D object that paints to the Tree JPanel
     * @param x             The xPos start of the rectangle this object can paint in.
     * @param y             The yPos start of the rectangle this object can paint in.
     * @param dimension     The dimension of the rectangle that represents this object.
     */
    public void drawInTree(final Graphics2D g2d, final int x, final int y, final int dimension){
        final Rectangle rectangle = new Rectangle(x,y,dimension,dimension);
        g2d.setColor(fillcolor);
        g2d.fill(rectangle);
        g2d.setColor(strokecolor);
        g2d.draw(rectangle);
    }
}
