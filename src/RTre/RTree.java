package RTre;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Author: Eirik Eide, Odin Hole Standal
 * Date: 04.okt.2004
 * Time: 13:27:50
 */
public final class RTree {
    // Parameters
    public int dataObjectWidth;         // Decides the maximum width of the boundingboxes
    public int dataObjectHeight;        // Decides the maximum height of the boundingboxes
    public int maxChildrenPerNode;      // M from the article on R-trees by Antonin Guttman
    public int minimumChildrenPerNode;  // m from the article on R-trees by Antonin Guttman

    private final ArrayList selected;
    private Node rootNode;
    private DataObject mostRecentDataObject;

    /**
     *    Initialises the variables
     */
    public RTree() {
        dataObjectHeight = 40;
        dataObjectWidth = 40;
        maxChildrenPerNode = 2;
        minimumChildrenPerNode = 1;
        selected = new ArrayList();
        rootNode = new Node(maxChildrenPerNode, minimumChildrenPerNode);
        rootNode.setLevel(1);
    }

    /**
     * Inserts an object in the tree. This method can insert any dataObject or subtree.
     *
     * @param element   The element that should be inserted.
     */
    public void insertTreeElement(final TreeElement element) {
        // I1 [Find position for new record] Invoke chooseNode to select a leaf node L
        // to place E (element).
        final Node l = chooseNode(element);
        Node ll = null;

        // I2 [Add record to leaf node] If L has room for another entry, install E (element).
        // Otherwise invoke splitNode to obtain  L and LL containing E an all the old entries of L.
        // AND
        if (!l.isFull()) {
            l.add(element);
        } else {
            ll = splitNode(l, element);
        }

        // I3 [Propagate changes upward] Invoke adjstTree on L, also passing LL if a split was
        // performed.
        final Node splitRoot = adjustTree(l, ll);

        // I4 [Grow tree taller] If node split propagation caused the root to split, create a new root whose
        // children are the two resulting nodes.
        if (splitRoot != null) {
            final Node newRoot = new Node(maxChildrenPerNode, minimumChildrenPerNode);
            newRoot.setLevel(rootNode.getLevel() + 1);
            newRoot.add(rootNode);
            newRoot.add(splitRoot);
            rootNode = newRoot;

        }
    }

    /**
     * Deletes all leaves in the tree.
     */

    public void deleteLeaves() {
        while (!selected.isEmpty()) {
            deleteLeaf((DataObject) selected.remove(0));
        }
    }

    /**
     * Deletes a given element from the tree.
     *
     * @param e The element that is to be deleted.
     */

    private void deleteLeaf(final DataObject e) {
        // D2 [Delete record]   NB!  D1 is done before this.
        // Remove E from L
        final Node l = e.getParent();
        l.removeChild(e);

        // D3 [Propagate changes]
        // Invoke condenseTree(), passing L
        condenseTree(l);

        // D4 [Shorten tree]
        // If the root node has only one child after the tree has been adjusted, make the child the new root
        if (rootNode.getSize() == 1 && rootNode.getLevel() > 1) {
            rootNode = (Node) rootNode.getChildren().get(0);
        }

    }

    /**
     * Searches the tree to find all elements that intersects a given rectangle. The elements are added
     * to the object's selection-list.
     *
     * @param r     The rectangle
     */

    public void findLeaves(final Rectangle r) {

        // FL1 [Search subtrees]
        // If T is not a leaf, check entry F in T to determine if F I overlaps E I.
        // For each such entry invoke findlead()
        // on the tree whose root is pointed to by F p until E is found or all entries have been checked

        // FL2 [Search leaf node for record]
        // If T is a leaf, check each entry to see if it matches E. If E is found return T.

        final LinkedList nodeQueue = new LinkedList();
        TreeElement currentNode;
        if (rootNode != null) {
            nodeQueue.add(rootNode);
        }

        while (!nodeQueue.isEmpty()) {
            currentNode = (TreeElement) nodeQueue.removeFirst();
            if (currentNode instanceof DataObject) {
                if (((DataObject) currentNode).intersects(r) || r.contains(currentNode.getBoundingBox())) {
                    selected.add(currentNode);
                    ((DataObject) currentNode).isSelected = true;
                }
            } else {
                if (currentNode.getBoundingBox().intersects(r)) {
                    nodeQueue.addAll(((Node) currentNode).getChildren());
                }
            }
        }
    }

    /**
     * Maintains the r-tree properies. Is used when deleting an element from the tree. Nodes containing less
     * than m children will be deleted. The deleted nodes' children are re-inserted into their original level.
     * The algorithm starts in a node and propagates upwards.
     *
     * @param l The startnode for the condense-algorithm
     */


    private void condenseTree(final Node l) {
        // CT1 [Initialize]
        Node n = l;
        Node p;
        final LinkedList q = new LinkedList();

        // CT2 [Find parent entry]

        while (!n.equals(rootNode)) {
            p = n.getParent();

            // CT3 [Eliminate under-full node]
            if (n.getSize() < minimumChildrenPerNode) {
                final Node parent = n.getParent();
                parent.removeChild(n);      // Delete the reference in the parent
                q.addAll(n.getChildren());
            }

            // CT4 [Adjust covering rectangle]
            // THIS IS DONE IN THE REMOVECHILD METHOD
            p.refreshBoundingBox();
            n.refreshBoundingBox();

            // CT5 [Move up one level in tree]
            n = p;
        }

        // CT6 [Re-insert orphaned entries]
        while (!q.isEmpty()) {
            insertTreeElement((TreeElement) q.removeLast());
        }

    }

    /**
     * Adjusts the nodes after insertion. Adjusts the bounding boxes of all ancestors of the inserted node.
     * If the node has split, the split-node will be inserted as well. If this results in two nodes at the root level,
     * the second one will be returned.
     *
     * @param l     The node where a child has been inserted.
     * @param ll    The result of a possible node split
     * @return      The node which will become a new root-nodes second child. (The old root node will become the first.)
     */

    private Node adjustTree(final Node l, final Node ll) {
        // AT1 [Initialise]
        // Set N=L. If L was split previously, set NN to be the resulting second node.
        Node n = l;
        Node nn = ll;

        // AT2 [Check if done]
        // If N is the root, stop.
        while (!n.equals(rootNode)) {
            // AT3 [Adjust covering rectangle in parent entry]
            // Let P be the parent node of N, and let En be N's entry in P. Adjust En I so that it thightly encloses
            // all entry rectangles in N.
            final Node p = n.getParent();

            p.refreshBoundingBox();

            // AT4 [Propagate node split upward]
            // If N has a partner NN resulting from an earlier split, create a new entry Enn with Enn P pointing to NN
            // and Enn I enclosing all rectangles in NN. Add Enn to P if there is room. Otherwise invoke splitNode to
            // produce P and PP containing Enn and all P's old entries.
            Node pp = null;
            if (nn != null) {
                nn.refreshBoundingBox();
                if (!p.isFull()) {
                    p.add(nn);
                } else {
                    pp = splitNode(p, nn);
                }
            }

            // AT5 [Move up to next level]
            // Set N = P and set NN == PP if a split occured. Repeat from AT2
            n = p;
            nn = pp;
        }
        return nn;
    }

    /**
     * Chooses the node where a dataobject or subtree should be inserted.
     *
     * @param e     A dataobject, or the node of a subtree, that is going to be inserted
     * @return      The node where the incoming element should be inserted
     */

    private Node chooseNode(final TreeElement e) {
        // Variables needed for this method
        Node bestNode, currentChildNode;
        int bestArea, currentArea;
        int bestEnlargement, currentEnlargement;

        // CL1 [Initialize] Set N to be the root node.
        Node n = rootNode;

        while (n.getLevel() > e.getLevel() + 1) {
            //CL2 [Leaf check] If N is a Leaf, return N.
            //CL3 [Choose subtree] If N is not a leaf, let F be the entry in N whose rectangle F I needs least enlargement
            //include E I. Resolve ties by choosing the entry with the rectangle of smallest area.
            // TODO: her kom det en feil!!! index out of bounds exception....
            bestNode = (Node) n.children.get(0);
            bestArea = calculateArea(bestNode.getBoundingBox());
            bestEnlargement = calculateEnlargement(bestNode, e);

            for (int i = 1; i < n.getSize(); i++) {
                currentChildNode = (Node) n.children.get(i);
                if (currentChildNode == null) break;
                currentEnlargement = (calculateEnlargement(currentChildNode, e));
                currentArea = calculateArea(currentChildNode.getBoundingBox());

                if (currentEnlargement < bestEnlargement) {
                    bestNode = currentChildNode;
                    bestArea = currentArea;
                    bestEnlargement = currentEnlargement;
                } else if (currentEnlargement == bestEnlargement) {
                    if (currentArea < bestArea) {
                        bestNode = currentChildNode;
                        bestArea = currentArea;
                        bestEnlargement = currentEnlargement;
                    }
                }
            }

            //CL4 [Descend until a leaf is reached.] Set N to be the child node pointed to by F p and repeat from CL2.
            n = bestNode;
        }
        return n;

    }

    /**
     * Calculates the area of a rectangle
     *
     * @param r The rectangle
     * @return  The rectangle's area
     */

    private int calculateArea(final Rectangle r) {
        return (r.width * r.height);

    }

    /**
     * Calculates the area of the boundingbox around two objects.
     *
     * @param p1    The first object
     * @param p2    The second object
     * @return      The resulting area
     */

    private int calculateArea(final TreeElement p1, final TreeElement p2) {
        final Rectangle p1Bounds = p1.getBoundingBox();
        final Rectangle p2Bounds = p2.getBoundingBox();

        final int[] xList = {p1Bounds.x,
                       p1Bounds.x + p1Bounds.width,
                       p1Bounds.x + p1Bounds.width,
                       p1Bounds.x,
                       p2Bounds.x,
                       p2Bounds.x + p2Bounds.width,
                       p2Bounds.x + p2Bounds.width,
                       p2Bounds.x
        };
        final int[] yList = {p1Bounds.y,
                       p1Bounds.y,
                       p1Bounds.y + p1Bounds.height,
                       p1Bounds.y + p1Bounds.height,
                       p2Bounds.y,
                       p2Bounds.y,
                       p2Bounds.y + p2Bounds.height,
                       p2Bounds.y + p2Bounds.height
        };

        final Polygon newBoundingBox = new Polygon(xList, yList, 8);

        return calculateArea(newBoundingBox.getBounds());

    }

    /**
     * Calculates the enlargement of inserting a node/data object into another node
     *
     * @param node      The node where the object is added
     * @param element   The object that is added
     * @return          The resulting enlargement of the object's bounding box
     */

    private int calculateEnlargement(final TreeElement node, final TreeElement element) {
        final int nodeArea = calculateArea(node.getBoundingBox());
        return calculateArea(node, element) - nodeArea;
    }

    /**
     * Implements quadratic split to split full nodes. That is a node that has more than M elements. The full node will
     * give some of it's children to a new node.
     *
     * @param fullNode      The node that is full.
     * @param addedNode     The node that there is not room for
     * @return              The new node from the split
     */

    private Node splitNode(final Node fullNode, final TreeElement addedNode) {

        // Gathers all the children in an ArrayList so that we can manipulate them more easily.
        final ArrayList nodes = new ArrayList(fullNode.children);
        nodes.add(addedNode);

        // QS1 [Pick first entry for each group]
        final ArrayList firstEntriesFirst = pickSeed(nodes);

        final Node l = new Node(maxChildrenPerNode, minimumChildrenPerNode);
        final Node ll = new Node(maxChildrenPerNode, minimumChildrenPerNode);

        l.setLevel(fullNode.getLevel());
        ll.setLevel(fullNode.getLevel());

        l.add((TreeElement) firstEntriesFirst.remove(0));
        ll.add((TreeElement) firstEntriesFirst.remove(0));

        // QS2 [Check if done] If all entries have been assigned, stop.
        while (!firstEntriesFirst.isEmpty()) {

            // QS2 [Check if done] If one group has so few entries that all the rest must be assigned
            // to it in order for it to have the minimum number m, assign them and stop.
            if ((minimumChildrenPerNode - l.children.size()) == firstEntriesFirst.size()) {
                while (!firstEntriesFirst.isEmpty()) {
                    l.add((TreeElement) firstEntriesFirst.remove(0));
                }
                break;
            }
            if ((minimumChildrenPerNode - ll.children.size()) == firstEntriesFirst.size()) {
                while (!firstEntriesFirst.isEmpty()) {
                    ll.add((TreeElement) firstEntriesFirst.remove(0));
                }
                break;
            }

            // QS3 [Select entry to assign] Invoke the algorithm pickNext to choose the next entry to assign.
            // Add it to the group whose covering rectangle will have to be enlarged least to accomodate it.
            // Resolve ties by adding the entry to the group with the smaller area, then to the one with fewer entries,
            // then to either. Repeat from QS2.
            final TreeElement bestPick = pickNext(l, ll, firstEntriesFirst);


            final int enlargement_l = calculateEnlargement(l, bestPick);
            final int enlargement_ll = calculateEnlargement(ll, bestPick);

            if (enlargement_l < enlargement_ll) {
                l.add(bestPick);
            } else if (enlargement_l == enlargement_ll) {
                final int area_l = calculateArea(l.getBoundingBox());
                final int area_ll = calculateArea(ll.getBoundingBox());

                if (area_l < area_ll) {
                    l.add(bestPick);
                } else if (area_l == area_ll) {
                    if (l.getSize() < ll.getSize()) {
                        l.add(bestPick);
                    } else {
                        ll.add(bestPick);
                    }
                } else {
                    ll.add(bestPick);
                }
            } else {
                ll.add(bestPick);
            }
        }
        // Updates the pointer to the now adjusted node.
        fullNode.morph(l);
        // Returns the second Node from the split.
        return ll;
    }

    /**
     * Method used in the split method to find a good split.
     *
     * @param l             The first node containing at least one child.
     * @param ll            The first node's splitnode containing at least one child.
     * @param remainingBB   The list of node that will be divided among the two nodes.
     * @return              The list of nodes that didn't get a parent
     */

    private TreeElement pickNext(final Node l, final Node ll, final ArrayList remainingBB) {

        TreeElement currentBB;
        int maxBB = 0;
        int currentdifference;
        int maxdifference = -1;

        int enlargement_l;
        int enlargement_ll;

        // PN1 [Determine cost of putting each entry in each group]
        // PN2 [Find entry with greatest preference for one group]

        for (int i = 0; i < remainingBB.size(); i++) {
            currentBB = (TreeElement) remainingBB.get(i);
            enlargement_l = calculateEnlargement(l, currentBB);
            enlargement_ll = calculateEnlargement(ll, currentBB);
            currentdifference = Math.abs(enlargement_l - enlargement_ll);
            if (currentdifference > maxdifference) {
                maxdifference = currentdifference;
                maxBB = i;
            }
        }

        return (TreeElement) remainingBB.remove(maxBB);
    }

    /**
     * Finds two seeds that will be in a node each after a split.
     *
     * @param nodes The list of available nodes.
     * @return      The two seeds
     */

    private ArrayList pickSeed(final ArrayList nodes) {
        // PS1 [Calculate inefficiency of grouping entries together]

        int mostWasteI = 0;
        int mostWasteU = 1;
        int mostWasteArea = 0;
        int currentWasteArea;

        for (int i = 0; i < nodes.size(); i++) {
            for (int u = i + 1; u < nodes.size(); u++) {
                currentWasteArea = calculateArea((TreeElement) nodes.get(i), (TreeElement) nodes.get(i));
                if (mostWasteArea < currentWasteArea) {
                    mostWasteI = i;
                    mostWasteU = u;
                    mostWasteArea = currentWasteArea;
                }
            }
        }

        final ArrayList mostWastePairFirst = new ArrayList();
        mostWastePairFirst.add(nodes.get(mostWasteI));
        mostWastePairFirst.add(nodes.get(mostWasteU));

        for (int i = 0; i < nodes.size(); i++) {
            if (!(i == mostWasteI || i == mostWasteU)) {
                mostWastePairFirst.add(nodes.get(i));
            }
        }

        return mostWastePairFirst;

    }

    /**
     *
     * Clears the current tree and inserts a new root node.
     *
     */

    public void clearTree() {
        releaseMostRecent();
        deselect();
        rootNode = new Node(maxChildrenPerNode, minimumChildrenPerNode);
        rootNode.setLevel(1);
    }

    /**
     * Getter method for the root node
     *
     * @return This tree's root node.
     */

    public Node getRootNode() {
        return rootNode;
    }

    /**
     * Empties the selection list
     *
     */

    public void deselect() {
        while (!selected.isEmpty()) {
            ((DataObject) selected.remove(0)).isSelected = false;
        }
    }

    /**
     * Sets a data object to be the one that is most recently inserted. Resets the former most recent object.
     *
     * @param newDataObject
     */

    public void mostRecent(final DataObject newDataObject) {
        releaseMostRecent();
        mostRecentDataObject = newDataObject;
        newDataObject.isMostRecent = true;
    }

    /**
     *  Releases the isMostrecent flag for the most recently inserted object.
     *
     */

    public void releaseMostRecent() {
        if (mostRecentDataObject != null) {
            mostRecentDataObject.isMostRecent = false;
            mostRecentDataObject = null;
        }


    }

    /**
     * Sets the colors of all objects in the tree.
     */

    public void setColor() {
        final ArrayList colorQueue = new ArrayList();
        colorQueue.add(rootNode);
        TreeElement bBox;

        while (!colorQueue.isEmpty()) {
            bBox = (TreeElement) colorQueue.remove(0);
            bBox.setColor();
            if (!(bBox instanceof DataObject)) {
                colorQueue.addAll(((Node) bBox).getChildren());
            }
        }
    }

    /**
     * Returns the number of levels in the tree
     *
     * @return Number of levels in the tree.
     */


    public int getNumberOfLevels() {
        return (rootNode.getLevel() + 1);
    }

    /**
     * Rebuilds the tree from scratch by collecting all data objects and then adding them one by one.
     *
     */

    public void rebuildTree() {
        final LinkedList dataObjects = new LinkedList();
        final LinkedList nodeQueue = new LinkedList();
        TreeElement currentElement;
        nodeQueue.add(rootNode);

        while (!nodeQueue.isEmpty()) {
            currentElement = (TreeElement) nodeQueue.removeFirst();
            if (currentElement instanceof DataObject) {
                currentElement.setParent(null);
                dataObjects.add(currentElement);
            } else {
                nodeQueue.addAll(((Node) currentElement).children);
            }
        }

        clearTree();

        while (!dataObjects.isEmpty()) {
            insertTreeElement((TreeElement) dataObjects.removeFirst());
        }
    }

}
