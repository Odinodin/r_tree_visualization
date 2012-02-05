package GUI.Listeners;

import GUI.PolygonPanel;

import javax.swing.event.MouseInputListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;

/**
 * Autho: Eirik Eide, Odin Hole Standal
 * Date: 06.okt.2004
 * Time: 15:39:09
 */
public final class SelectionListener implements MouseInputListener  {
    private int startX = -1;
    private int startY = -1;
    private final PolygonPanel pPanel;

    public SelectionListener(final PolygonPanel pPanel){
        this.pPanel = pPanel;
    }

    public void mouseClicked(final MouseEvent e) {
    }

    public void mousePressed(final MouseEvent e) {
        startX = e.getX();
        startY = e.getY();

        pPanel.selectionStarted();
    }

    public void mouseReleased(final MouseEvent e) {
        //Release the mousepressed flag
        pPanel.selectNodes();
        startX = -1;
        startY = -1;
        pPanel.mousePressed = false;
    }

    public void mouseEntered(final MouseEvent e) {
    }

    public void mouseExited(final MouseEvent e) {
    }

    public void mouseDragged(final MouseEvent e) {
        if (startX!=-1){
            pPanel.calculateSelectionRectangle(startX,startY,e.getX(),e.getY());
        }
    }

    public void mouseMoved(final MouseEvent e) {
    }
}
