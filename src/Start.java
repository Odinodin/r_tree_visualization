
import RTre.RTree;
import GUI.GUI_Controller;

/**
 * The class where the main method is. This is where you start the program.
 *
 * Authors: Odin Hole Standal, Eirik Eide
 * Date: 04.okt.2004
 * Time: 12:18:10
 */
public final class Start {

    /**
    * The main method.
    */
    public static void main(final String[] args) {
        // Starts up the GUI and instanciates a new RTree object.
        new GUI_Controller(new RTree());

    }
}
