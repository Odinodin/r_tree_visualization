package RTre;

import java.awt.*;
import java.util.ArrayList;

/**
 * This interface is used by Nodes and DataObjects to allow them to execute functionality
 * that they have in common. This may for instance be when we are drawing nodes and dataobjects from
 * the same list of TreeElements.
 *
 * Authors: Eirik Eide, Odin Hole Standal
 * Date: 05.okt.2004
 * Time: 11:35:23
 */
public interface TreeElement {

    public Node getParent();
    public void setParent(Node parent);
    public Rectangle getBoundingBox();
    public void drawMe(Graphics2D g2d);
    public int getLevel();
    public void setColor();

}
