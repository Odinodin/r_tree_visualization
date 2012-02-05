package GUI.Listeners;
import RTre.RTree;
import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import GUI.Main;

/**
 * Author: Eirik Eide, Odin Hole Standal
 * Date: 08.okt.2004
 * Time: 09:27:13
 */
public final class HeightAndWidthListener implements ActionListener{
    private final RTree rtree;
    private final String boxId;
    private final Main main;


    public HeightAndWidthListener(final RTree rtree, final Main main, final String boxId) {
        this.rtree = rtree;
        this.main = main;
        this.boxId = boxId;
    }

    public void actionPerformed(final ActionEvent e) {
        final JComboBox cb = (JComboBox)e.getSource();

        final int selectedValue = ((Integer)cb.getSelectedItem()).intValue();

        if (boxId.equals("height")){
            rtree.dataObjectHeight = selectedValue;
        } else if(boxId.equals("width")) {
            rtree.dataObjectWidth = selectedValue;
        }
        // Update the GUI
        main.updateGUI();
    }
}
