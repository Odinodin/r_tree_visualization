package RTre;

import java.awt.*;
import java.util.ArrayList;

/**
 * Nodes are conceptually the same as the nodes in the RTree article. This class encapsulates
 * the necessary variables and methods that the RTree needs to manipulate the tree-structure.
 *
 * Authors: Eirik Eide, Odin Hole Standal
 * Date: 04.okt.2004
 * Time: 13:28:17
 */
public final class Node implements TreeElement{
    
    private final int maxChildrenPerNode;

    private Node parent;
    public ArrayList children;
    private Rectangle boundingBox;
    public Color fillcolor;
    private Color strokecolor;
    private int level;

     /**
     * Constructor
     * Initialises the variables
     *
     * @param max   The maximum number of children this node can have.
     * @param min   The minimum number of children this node can have.
     */
    public Node(final int max,final int min){
        maxChildrenPerNode = max;
        // Initialiseres the children-array
        children = new ArrayList();
    }

    /**
     * Returns the parent of this Node
     *
     * @return  this nodes Parent
     */
    public Node getParent(){
        return parent;
    }


    	/**
     * Sets the parent of this node
     *
     * @param parent    The new parent
     */
    public void setParent(final Node parent){
        this.parent = parent;
    }


     /**
     * Returns the boundingbox associated with this Node
     *
     * @return  the boundingbox of this Node
     */
    public Rectangle getBoundingBox(){
        return boundingBox;
    }


    /**
     * Adds a TreeElement to this Nodes children-list
     *
     * @param element   the TreeElemtn to be added to this Nodes children
     */
    public void add(final TreeElement element) {
        children.add(element);
        if (!(element instanceof DataObject)){
           ((Node)element).setLevel(level-1);
        }
        element.setParent(this);
        updateBoundingBox(element.getBoundingBox());
    }

    /**
     * Checks if the children array is full.
     *
     * @return  true if this Node has maximum number of children
     */
    public boolean isFull() {
        if (children.size() == maxChildrenPerNode){
            return true;
        } else return false;
    }

    /**
     * Removes the specified TreeElemtent from the children list
     *
     * @param e     The child to be removed
     */
    public void removeChild(final TreeElement e) {
        children.remove(e);
        e.setParent(null);
        refreshBoundingBox();
    }


    /**
     * Refreshes the boundingBox of this Node based on the children in the children-array.
     * This is done each time it is necessary (when a child is added or removed)
     */
    public void refreshBoundingBox() {

        if (children.size() == 0){
            boundingBox = null;
        }
        else {
            boundingBox = ((TreeElement)children.get(0)).getBoundingBox();

            int minimum_x = boundingBox.x;
            int maximum_x = boundingBox.x+boundingBox.width;
            int minimum_y = boundingBox.y;
            int maximum_y = boundingBox.y+boundingBox.height;

            Rectangle current;
            for (int i=0; i<children.size(); i++){
                current = ((TreeElement)children.get(i)).getBoundingBox();
                if(current.x < minimum_x){
                    minimum_x = current.x;
                }
                if( (current.x + current.width) > maximum_x){
                    maximum_x = current.x + current.width;
                }
                if(current.y < minimum_y){
                    minimum_y = current.y;
                }
                if( (current.y + current.height) > maximum_y){
                    maximum_y = current.y + current.height;
                }
            }
            // Updates the boundingbox
            boundingBox = new Rectangle(minimum_x,minimum_y,maximum_x-minimum_x,maximum_y-minimum_y);
        }

    }


     /**
     * Updates the boundingbox of this Node after the insertion of a new element.
     *
     * @param newBB     The rectangel which the boundingbox should now incorporate
     */
    private void updateBoundingBox(final Rectangle newBB){
        if (boundingBox == null){
            boundingBox = newBB;
        } else {
            int minimum_x = boundingBox.x;
            int maximum_x = boundingBox.x+boundingBox.width;
            int minimum_y = boundingBox.y;
            int maximum_y = boundingBox.y+boundingBox.height;

            if(newBB.x < minimum_x){
                minimum_x = newBB.x;
            }
            if( (newBB.x + newBB.width) > maximum_x){
                maximum_x = newBB.x + newBB.width;
            }
            if(newBB.y < minimum_y){
                minimum_y = newBB.y;
            }
            if( (newBB.y + newBB.height) > maximum_y){
                maximum_y = newBB.y + newBB.height;
            }
            boundingBox = new Rectangle(minimum_x,minimum_y, maximum_x-minimum_x,maximum_y-minimum_y);
        }
    }

     /**
     * Draws the node in the PolygonView Panel (the bottomost panel).
     * The drawing is based on the boundingbox rectangle of this Node.
     *
     * @param g2d       The graphics Object that paints to the PolygonView Panel
     */
    public void drawMe(final Graphics2D g2d){

        if (boundingBox !=null){
            g2d.setColor(strokecolor);
            g2d.draw(boundingBox);
            g2d.setColor(fillcolor);
            g2d.fill(boundingBox);
        }
    }

    /**
     * Returns the number of children this node has
     *
     * @return  The number of children this Node has
     */
    public int getSize(){
        return children.size();
    }


    /**
     * Returns the childlist of this Node.
     *
     * @return      the list which holds this node's children.
     */
    public ArrayList getChildren(){
        return children;
    }


    /**
     * Returns the level of this Node
     *
     * @return  The level of this Node
     */
    public int getLevel(){
        return level;
    }


    /**
     * Gives this Node a new level
     *
     * @param level    The new level of this node
     */
    public void setLevel(final int level){
        this.level = level;
    }


    /**
     * Morphs this node into the parameter Node. By morhping we mean taking all the
     * variables of the parameter node and updating this Nodes variables.
     *
     * @param l     The node this node is going to become
     */
    public void morph(final Node l) {
        children = l.children;

        for(int i = 0; i < getSize(); i++){
            ((TreeElement) children.get(i)).setParent(this);
        }

        boundingBox = null;
        refreshBoundingBox();
    }


    /**
     * Assigns a color to this Node based on its level
     */
    public void setColor(){
        if (getLevel() == 1){
           if(strokecolor == null){
           strokecolor = safeColor();
           fillcolor = new Color(strokecolor.getRed(),strokecolor.getGreen(),strokecolor.getBlue(),80);
           }
        } else{
            int shade = 150+level*5;
            if (shade > 250){
                shade = 250;
            }
            strokecolor = new Color(shade,shade,shade, 200);
            fillcolor = new Color(shade,shade,shade,20);
        }
    }


    /**
     * We use this method to constrain the possible colors that can be assigned to a Node.
     * This is purely for aesthetics.
     *
     * @return  a safe Color
     */
    private Color safeColor(){
        final ArrayList colors = new ArrayList();
        colors.add(new Integer((int)(Math.random()*50)));
        colors.add(new Integer((int)(Math.random()*50+190)));
        colors.add(new Integer((int)(Math.random()*240)));

        return new Color(((Integer)colors.remove( (int)( Math.random()*colors.size( ) ))).intValue(),
                ((Integer)colors.remove( (int)( Math.random()*colors.size( ) ))).intValue(),
                ((Integer)colors.remove( (int)( Math.random()*colors.size( ) ))).intValue(),
                160);

    }


    /**
     * Paints this Node in the correct position in the TreeView Panel
     *
     * @param g2d       The graphics Object which paints to the TreeView Panel
     * @param x         The xPos start of the box which this Node can draw in
     * @param y         The yPos start of the box which this Node can draw in
     * @param width     The width of the box this node is allowed to draw in.
     * @param heigth    The height of the box this node is allowed to draw in.
     * @param dimension The dimension of the rectangle that represents this node in the tree
     */
    public void drawInTree(final Graphics2D g2d, final int x, final int y, final int width, final int heigth, final int dimension){
        //Calculate where the node shall be drawn
        final int nodeStartX = x+width/2 - (maxChildrenPerNode*dimension)/2;
        //Calculate the width of the childboxes
        final int childWidth = width/maxChildrenPerNode;
        //Calculate where the start of the first child-pointer
        int pointerStartX = nodeStartX+(dimension/2);
        //Calculate where the end of the first child-pointer
        int pointerEndX = x + (childWidth / 2);

        final Rectangle rectangle = new Rectangle(nodeStartX,y,dimension,dimension);

        //Drawing the node and it's pointers
        for (int i = 0; i < maxChildrenPerNode; i++){

            g2d.setColor(strokecolor);
            g2d.draw(rectangle);
            g2d.drawLine(pointerStartX,(y+dimension),pointerEndX,y+heigth);

            rectangle.translate(dimension,0);
            pointerStartX+=dimension;
            pointerEndX+=childWidth;
        }
    }
}
