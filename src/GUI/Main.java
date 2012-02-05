package GUI;

import RTre.RTree;
import RTre.DataObject;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import GUI.Listeners.*;

/**
 * The IntelliJ visual GUI-editor is bound to this class. When you add an element in
 * the visual editor you can bind it to a variable in this class. This variable acts
 * just as if you shold have added it to the GUI manually. You can then add models,
 * set the elements parameters and so on. This is what is done in this class.
 *
 * Authors: Eirik Eide, Odin Hole Standal
 * Date: 04.okt.2004
 * Time: 14:09:58
 */
public final class Main {
     // The outermost JPanel. Need a reference to it to add it to the JFrame
    public JPanel mainPanel;

    // Paneles where we redefine the paint() method
    private TreeViewer treeViewerPanel;     // Extends JPanel
    private PolygonPanel polygonDrawPanel;  // Extends JPanel
    private JScrollPane polygonScroller;    // The polygon-panel scroller
    private JScrollPane treeScroller;       // The tree-panel scroller

    // JButtons
    private JButton delete;
    private JButton clear;
    private JButton add;
    private final RTree rtree;
    private JComboBox addNumber;
    private JButton help;
    private JComboBox combo_M;
    private JComboBox combo_m;
    private JComboBox combo_width;
    private JComboBox combo_height;


    /**
     * Constructor
     * @param rtree     A reference to the rtree so that the GUI has access to its values and methods
     */
    public Main(final RTree rtree){
        this.rtree = rtree;

        setup_treeScroller();
        setup_polygonScroller();
        setup_buttons();
        setup_comboboxes();
    }


    /**
     * Assigns models to all the comboboxes in the GUI.
     */
    private void setup_comboboxes() {
        ComboBoxModel model;

        // ADD - combobox
        final Integer[] addValues = {new Integer(1),
                               new Integer(2),
                               new Integer(5),
                               new Integer(10),
                               new Integer(20),
                               new Integer(50),
                               new Integer(100),
                               new Integer(2000),
        };
        model = new DefaultComboBoxModel(addValues);
        addNumber.setModel(model);


        // M - combobox
        final Integer[] MValues = {new Integer(2),
                             new Integer(4),
                             new Integer(6),
                             new Integer(8),
                             new Integer(10),
                             new Integer(16),
        };
        model = new DefaultComboBoxModel(MValues);
        combo_M.setModel(model);
        combo_M.setSelectedIndex(0);
        MAndmListener mAndMListener = new MAndmListener(rtree, this, "M");
        combo_M.addActionListener(mAndMListener);


        // m - combobox
        final Integer[] mValues = {new Integer(1)};
        model = new DefaultComboBoxModel(mValues);
        combo_m.setModel(model);
        mAndMListener = new MAndmListener(rtree, this, "m");
        combo_m.addActionListener(mAndMListener);


        // Width - combobox
        final Integer[] widthValues = {new Integer(10),
                                 new Integer(20),
                                 new Integer(40),
                                 new Integer(60),
                                 new Integer(80),
                                 new Integer(100),
                                 new Integer(200),
                                 new Integer(400),
                                 new Integer(600),
        };
        model = new DefaultComboBoxModel(widthValues);
        combo_width.setModel(model);
        combo_width.setSelectedIndex(2);
        // Adds an Actionlistener
        HeightAndWidthListener hwListener = new HeightAndWidthListener(rtree, this, "width");
        combo_width.addActionListener(hwListener);

        // Height - combobox
        final Integer[] heightValues = {new Integer(10),
                                  new Integer(20),
                                  new Integer(40),
                                  new Integer(60),
                                  new Integer(80),
                                  new Integer(100),
                                  new Integer(200),
                                  new Integer(300),
        };
        model = new DefaultComboBoxModel(heightValues);
        combo_height.setModel(model);
        combo_height.setSelectedIndex(2);
        // Adds an Actionlistener
        hwListener = new HeightAndWidthListener(rtree, this, "height");
        combo_height.addActionListener(hwListener);
    }


    /**
     * Assigns a new polygonDraw'er (which extends JPanel) to the polygon-scrollPane so that we
     * can paint a tree in it.
     */
    private void setup_polygonScroller() {
        polygonDrawPanel = new PolygonPanel(rtree, this);
        polygonScroller.setViewportView(polygonDrawPanel);
        polygonScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        polygonScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    }


    /**
     * Assigns a new TreeViewer (which extends JPanel) to the tree-scrollPane so that we
     * can paint a tree in it.
     */
    private void setup_treeScroller() {
        treeViewerPanel = new TreeViewer(rtree);
        treeScroller.setViewportView(treeViewerPanel);
    }


    /**
     * Adds actionlisteners to all the buttons on the MainPanel so that the appropriate action
     * is invoked when the buttons are pushed.
     */
    private void setup_buttons() {
        // The ADD button
        add.addActionListener(new ActionListener(){
            public void actionPerformed(final ActionEvent e) {
                rtree.deselect();
                for(int i = 0; i < ((Integer)addNumber.getSelectedItem()).intValue(); i++){
                    final DataObject newDataObject = new DataObject(rtree);
                    rtree.mostRecent(newDataObject);
                    rtree.insertTreeElement(newDataObject);
                }
                updateGUI();
            }
        });


        // The CLEAR button
        clear.addActionListener(new ActionListener(){
            public void actionPerformed(final ActionEvent e) {
                rtree.clearTree();
                updateGUI();
            }
        });


        // The DELETE button
        delete.addActionListener(new ActionListener(){
            public void actionPerformed(final ActionEvent e) {
                rtree.releaseMostRecent();
                rtree.deleteLeaves();
                updateGUI();
            }
        });

        //The HELP button
        help.addActionListener(new ActionListener(){
            public void actionPerformed(final ActionEvent e) {
                treeViewerPanel.toggleHelp();
            }
        });

    }


    /**
     * Updates the colors in the tree and refreshes the TreeView and the PolygonView.
     */
    public void updateGUI(){
        treeViewerPanel.hideHelp();
        rtree.setColor();
        treeViewerPanel.adjust();
        polygonDrawPanel.repaint();
    }


    /**
     * Updates the the little m-combobox so that the value is the floored value of M/2.
     * Also, the rtree-m value is updated to reflect the largest value of the combobox-value
     *
     * @param Mvalue    Calculate the new m-value from this value.
     */
    public void update_mComboBox(final int Mvalue){
        // Calculate the new max value for the little m range
        final int max_mValue = Mvalue/2;

        // Add all the values up and to the new maxvalue.
        final Integer[] newValues = new Integer[max_mValue];
        int i;
        for (i=0; i<max_mValue; i++){
            newValues[i] = new Integer(i+1);
        }
        // Update the m-value
        rtree.minimumChildrenPerNode = newValues[i-1].intValue();

        // Update the combobox
        final DefaultComboBoxModel modell = new DefaultComboBoxModel(newValues);
        combo_m.setModel(modell);
        combo_m.setSelectedIndex(i-1);
    }
}
