package org.basex.data;

/**
 * This class gives the ID-Pre-Mapping. The implementation uses two
 * Red-Black-Trees. One to keep up with the changes in the pre-values,
 * the other one to map the IDs to these changes.
 *
 * For details please look at my Bachelor-thesis.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Philipp Ziemer, Philipp.Ziemer@uni-konstanz.de
 *
 */
public class MapTree {
    /** Root of the preTree. */
    private MapNode preRoot;
    /** Root of the idTree. */
    private MapNode idRoot;
    /** Pointer to the currently viewed map-node in the preTree. */
    private MapNode current;
    /** Added addend for map-node current. */
    int sadd;

    /**
     * Initialize a mapping with the original document.
     * @param id The largest id in the document.
     */
    public MapTree(final int id) {
      // create MapNode containing original document
      MapNode root  = new MapNode();
      root.idmax    = id;
      // insert into trees
      preRoot       = root;
      idRoot        = root;
      root.preColor = true;
      root.idColor  = true;

      // set the pointer
      current = null;
      sadd    = 0;
    }


    // **************************************************** //
    // ******* preTree ******* //
    // *********************** //

    // ***** Rotation ***** //

    /**
     * Rotate the MapNode in the PreeTree to the left.
     * The right child-node becomes the new parent-node of the node.
     * @param node MapNode to rotate.
     */
    private void rotatePreLeft(final MapNode node) {
        MapNode right = node.preRight;
        replacePreNode(node, right);
        node.preRight = right.preLeft;
        if (right.preLeft != null) {
            right.preLeft.preParent = node;
        }
        right.preLeft = node;
        node.preParent = right;
        right.s += node.s;
    }

    /**
     * Rotate the MapNode in the preTree to the right.
     * The left child-node becomes the new parent-node of the node.
     * @param node MapNode to rotate.
     */
    private void rotatePreRight(final MapNode node) {
        MapNode left = node.preLeft;
        replacePreNode(node, left);
        node.preLeft = left.preRight;
        if (left.preRight != null) {
            left.preRight.preParent = node;
        }
        left.preRight = node;
        node.preParent = left;
        node.s -= left.s;
    }

    /**
     * Replaces a given MapNode with another one in the preTree.
     * @param oldNode The old MapNode.
     * @param newNode The MapNode to take oldNode's position.
     */
    private void replacePreNode(final MapNode oldNode, final MapNode newNode) {
        if (oldNode.preParent == null) {
            preRoot = newNode;
        } else {
            if (oldNode == oldNode.preParent.preLeft)
                oldNode.preParent.preLeft = newNode;
            else
                oldNode.preParent.preRight = newNode;
        }
        if (newNode != null) {
            newNode.preParent = oldNode.preParent;
        }
    }

    // ***** help methods ***** //

    /**
     * Help method to calculate the min-value (left border) of the
     * pre-interval of the currently viewed node.
     * @return Min-value of the pre-interval
     */
    private int preMin() {
      return current.idmin + current.d + current.s + sadd;
    }

    /**
     * Help method to calculate the max-value (right border) of the
     * pre-interval of the currently viewed node.
     * @return Max-value of the pre-interval
     */
    private int preMax() {
      return current.idmax + current.d + current.s + sadd;
    }

    /**
     * Get color of node in preTree.
     * @param node MapNode needed color for.
     * @return False for color red or true for color black.
     */
    protected static boolean nodePreColor(final MapNode node) {
      return node == null ? true : node.preColor;
    }

    /**
     * Search the preTree for the value pre. Modify the values of the
     * visited mapNodes, point current to the searched node and save
     * the total value sadd.
     * @param pre value of the searched mapNode.
     * @param mod the modifier for the visited nodes in a step left.
     */
    private void searchPreTree(final int pre, final int mod) {
      current = preRoot;
      sadd = 0;

      // search for MapNode to divide
      while(current != null && !(preMin() <= pre && pre <= preMax())) {
        if(pre < preMin()) {
          // go left modify addend of current node
          current.s += mod;
          current    = current.preLeft;
        } else {
          // go right, count addend
          sadd    += current.s;
          current  = current.preRight;
        }
      }
    }

    // ***** insert ***** //

    /**
     * Inserts a MapNode as the successor of current.
     * @param newnode The new MapNode to insert.
     */
    private void insertPreTree(final MapNode newnode) {
      // add as right child
      if(current.preRight == null) {
        current.preRight  = newnode;
        newnode.preParent = current;
        preInsertCase1(newnode);
        return;
      }

      // add as successor
      MapNode node = current.preRight;
      while(node.preLeft != null) {
        node.s += newnode.s;
        node    = node.preLeft;
      }
      node.preLeft      = newnode;
      node.s           += newnode.s;
      newnode.preParent = node;
      preInsertCase1(newnode);
    }

    /**
     * Recursive handle for insert case 1. Calls case 2.
     * @param node Viewed node.
     */
    private void preInsertCase1(final MapNode node) {
        if (node.preParent == null)
            node.preColor = true;
        else
            preInsertCase2(node);
    }

    /**
     * Recursive handle for insert case 2. Calls case 3.
     * @param node Viewed node.
     */
    private void preInsertCase2(final MapNode node) {
        if (nodePreColor(node.preParent))
            return; // Tree is still valid

        preInsertCase3(node);
    }

    /**
     * Recursive handle for insert case 3. Calls case 1 if it checks, else
     * case 4.
     * @param node Viewed node.
     */
    private void preInsertCase3(final MapNode node) {
        if (!nodePreColor(node.preUncle())) {
            node.preParent.preColor = true;
            node.preUncle().preColor = true;
            node.preGrandparent().preColor = false;
            preInsertCase1(node.preGrandparent());
        } else {
            preInsertCase4(node);
        }
    }

    /**
     * Recursive handle for insert case 4. Calls case 5.
     * @param node Viewed node.
     */
    private void preInsertCase4(final MapNode node) {
        MapNode child = node;

        if (node == node.preParent.preRight
              && node.preParent == node.preGrandparent().preLeft) {
            rotatePreLeft(node.preParent);
            child = node.preLeft;
        } else if (node == node.preParent.preLeft
              && node.preParent == node.preGrandparent().preRight) {
            rotatePreRight(node.preParent);
            child = node.preRight;
        }
        preInsertCase5(child);
    }

    /**
     * Recursive handle for insert case 5. Last step.
     * @param node Viewed node.
     */
    private void preInsertCase5(final MapNode node) {
        node.preParent.preColor = true;
        node.preGrandparent().preColor = false;
        if (node == node.preParent.preLeft
              && node.preParent == node.preGrandparent().preLeft) {
            rotatePreRight(node.preGrandparent());
        } else {
            assert node == node.preParent.preRight
              && node.preParent == node.preGrandparent().preRight;
            rotatePreLeft(node.preGrandparent());
        }
    }

    // ***** delete ***** //

    /**
     * Delete a MapNode from the preTree. It is replaced with it's successor
     * and then delete that one instead.
     * @param node The MapNode to delete.
     */
    private void deletePreTree(final MapNode node) {
        MapNode suc = node;

        if(node.preRight != null) {
          suc = preMinimumNode(node.preRight);

          // copy values from successor into a new node
          MapNode newnode = new MapNode();
          newnode.idmin   = suc.idmin;
          newnode.idmax   = suc.idmax;
          newnode.d   = suc.d;
          newnode.s       = suc.s + node.s; // successor inherits addend

          // replace node with newnode in preTree
          replacePreNode(node, newnode);
          newnode.preLeft  = node.preLeft;
          newnode.preRight = node.preRight;
          newnode.preColor = node.preColor;
          if(node.preLeft != null)
            node.preLeft.preParent = newnode;
          if(node.preRight != null) {
            node.preRight.preParent = newnode;
            // adjust the addend for subtree
            MapNode subnode = node.preRight;
            while(subnode != null) {
              subnode.s -= newnode.s;
              subnode    = subnode.preLeft;
            }
          }

          // replace suc with newnode in idTree
          replaceNodeIdTree(suc, newnode);
          newnode.idLeft  = suc.idLeft;
          newnode.idRight = suc.idRight;
          newnode.idColor = suc.idColor;
          if(suc.idLeft != null)
            suc.idLeft.idParent  = newnode;
          if(suc.idRight != null)
            suc.idRight.idParent = newnode;
        }

        assert suc.preLeft == null || suc.preRight == null;
        MapNode child = (suc.preLeft == null) ? suc.preRight : suc.preLeft;
        if(nodePreColor(suc)) {
          suc.preColor = nodePreColor(child);
          preDeleteCase1(suc);
        }
        replacePreNode(suc, child);

        if(!nodePreColor(preRoot))
          preRoot.preColor = true;
    }

    /**
     * Returns the minimal MapNode in the pre-subtree given by the root.
     * @param root The root of the subtree.
     * @return The MapNode with the lowest pre-values.
     */
    private static MapNode preMinimumNode(final MapNode root) {
        assert root != null;
        MapNode node = root;

        while(node.preLeft != null)
          node = node.preLeft;

        return node;
    }

    /**
     * Recursive handle for delete case 1. Calls case 2.
     * @param node Viewed node.
     */
    private void preDeleteCase1(final MapNode node) {
        if(node.preParent == null)
            return;

        preDeleteCase2(node);
    }

    /**
     * Recursive handle for delete case 2. Calls case 3.
     * @param node Viewed node.
     */
    private void preDeleteCase2(final MapNode node) {
        if(!nodePreColor(node.preSibling())) {
            node.preParent.preColor = false;
            node.preSibling().preColor = true;
            if(node == node.preParent.preLeft)
                rotatePreLeft(node.preParent);
            else
                rotatePreRight(node.preParent);
        }
        preDeleteCase3(node);
    }

    /**
     * Recursive handle for delete case 3. Calls case 1 and 4.
     * @param node Viewed node.
     */
    private void preDeleteCase3(final MapNode node) {
        if(nodePreColor(node.preParent) &&
            nodePreColor(node.preSibling()) &&
            nodePreColor(node.preSibling().preLeft) &&
            nodePreColor(node.preSibling().preRight)) {
            node.preSibling().preColor = false;
            preDeleteCase1(node.preParent);
        } else
            preDeleteCase4(node);
    }

    /**
     * Recursive handle for delete case 4. Calls case 2.
     * @param node Viewed node.
     */
    private void preDeleteCase4(final MapNode node) {
        if(!nodePreColor(node.preParent) &&
            nodePreColor(node.preSibling()) &&
            nodePreColor(node.preSibling().preLeft) &&
            nodePreColor(node.preSibling().preRight)) {
            node.preSibling().preColor = false;
            node.preParent.preColor = true;
        } else
            preDeleteCase5(node);
    }

    /**
     * Recursive handle for delete case 5. Calls case 6.
     * @param node Viewed node.
     */
    private void preDeleteCase5(final MapNode node) {
        if(node == node.preParent.preLeft &&
            nodePreColor(node.preSibling()) &&
            !nodePreColor(node.preSibling().preLeft) &&
            nodePreColor(node.preSibling().preRight)) {
            node.preSibling().preColor = false;
            node.preSibling().preLeft.preColor = true;
            rotatePreRight(node.preSibling());
        } else if(node == node.preParent.preRight &&
                 nodePreColor(node.preSibling()) &&
                 !nodePreColor(node.preSibling().preRight) &&
                 nodePreColor(node.preSibling().preLeft)) {
            node.preSibling().preColor = false;
            node.preSibling().preRight.preColor = true;
            rotatePreLeft(node.preSibling());
        }
        preDeleteCase6(node);
    }

    /**
     * Recursive handle for delete case 6.
     * @param node Viewed node.
     */
    private void preDeleteCase6(final MapNode node) {
        node.preSibling().preColor = nodePreColor(node.preParent);
        node.preParent.preColor = true;
        if(node == node.preParent.preLeft) {
            assert !nodePreColor(node.preSibling().preRight);
            node.preSibling().preRight.preColor = true;
            rotatePreLeft(node.preParent);
        } else {
            assert !nodePreColor(node.preSibling().preLeft);
            node.preSibling().preLeft.preColor = true;
            rotatePreRight(node.preParent);
        }
    }

    // **************************************************** //
    // ******* idTree ******* //
    // ********************** //

    // ***** rotation ***** //

    /**
     * Rotate the MapNode in the idTree to the left.
     * The right child-node becomes the new parent-node of the node.
     * @param node MapNode to rotate.
     */
    private void rotateIdLeft(final MapNode node) {
      MapNode right = node.idRight;
        replaceNodeIdTree(node, right);
        node.idRight = right.idLeft;
        if(right.idLeft != null)
            right.idLeft.idParent = node;
        right.idLeft = node;
        node.idParent = right;
    }

    /**
     * Replaces a given MapNode with another one in the idTree.
     * @param node MapNode to rotate.
     */
    private void rotateIdRight(final MapNode node) {
        MapNode left = node.idLeft;
        replaceNodeIdTree(node, left);
        node.idLeft = left.idRight;
        if(left.idRight != null)
            left.idRight.idParent = node;
        left.idRight = node;
        node.idParent = left;
    }

    /**
     * Replaces a given MapNode with another one in the idTree.
     * @param oldNode The old MapNode.
     * @param newNode The MapNode to take oldNode's position.
     */
    private void replaceNodeIdTree(final MapNode oldNode,
        final MapNode newNode) {
      if(oldNode.idParent == null)
            idRoot = newNode;
        else {
            if(oldNode == oldNode.idParent.idLeft)
                oldNode.idParent.idLeft = newNode;
            else
                oldNode.idParent.idRight = newNode;
        }
        if(newNode != null)
            newNode.idParent = oldNode.idParent;
    }

    // ***** help methods ***** //

    /**
     * Get color of node in idTree.
     * @param node MapNode needed color for.
     * @return False for color red or true for color black.
     */
    protected static boolean nodeIdColor(final MapNode node) {
        return node == null ? true : node.idColor;
    }

    /**
     * Search the idTree for the value id.
     * @param id value of the searched MapNode.
     * @return MapNode with id in it's id-interval.
     */
    private MapNode searchIdTree(final int id) {
      MapNode node = idRoot;
      while(node != null) {
        if(id < node.idmin)
          node = node.idLeft;
        else if(node.idmax < id)
          node = node.idRight;
        else {
          assert node.idmin <= id && id <= node.idmax;
          return node;
        }
      }
      return null;
    }

    // ***** insert ***** //

    /**
     * Inserts a MapNode in the idTree.
     * @param newNode The new MpNode to insert.
     */
    private void insertIdTree(final MapNode newNode) {
        MapNode node = idRoot;

      while (true) {
            // check if new node is in interval => error!
          assert node.idmin <= newNode.idmin || newNode.idmax <= node.idmax;

          // go left
            if(newNode.idmax < node.idmin) {
                if(node.idLeft == null) {
                    node.idLeft = newNode;
                    break;
                }
                node = node.idLeft;
            // go right
            } else {
                // check for error
              assert node.idmax < newNode.idmin;
                if(node.idRight == null) {
                    node.idRight = newNode;
                    break;
                }
                node = node.idRight;
            }
        }
        newNode.idParent = node;

        // rebalance idTree
        idInsertCase1(newNode);
    }

    /**
     * Recursive handle for insert case 1. Calls case 2.
     * @param node Viewed node.
     */
    private void idInsertCase1(final MapNode node) {
        if(node.idParent == null)
            node.idColor = true;
        else
            idInsertCase2(node);
    }

    /**
     * Recursive handle for insert case 2. Calls case 3.
     * @param node Viewed node.
     */
    private void idInsertCase2(final MapNode node) {
        if(nodeIdColor(node.idParent))
            return; // Tree is still valid

        idInsertCase3(node);
    }

    /**
     * Recursive handle for insert case 3. Calls case 1 if it checks,
     * else case 4.
     * @param node Viewed node.
     */
    private void idInsertCase3(final MapNode node) {
        if(!nodeIdColor(node.idUncle())) {
            node.idParent.idColor = true;
            node.idUncle().idColor = true;
            node.idGrandparent().idColor = false;
            idInsertCase1(node.idGrandparent());
        } else {
            idInsertCase4(node);
        }
    }

    /**
     * Recursive handle for insert case 4. Calls case 5.
     * @param node Viewed node.
     */
    private void idInsertCase4(final MapNode node) {
        MapNode child = node;

        if(node == node.idParent.idRight
              && node.idParent == node.idGrandparent().idLeft) {
            rotateIdLeft(node.idParent);
            child = node.idLeft;
        } else if (node == node.idParent.idLeft
              && node.idParent == node.idGrandparent().idRight) {
            rotateIdRight(node.idParent);
            child = node.idRight;
        }
        idInsertCase5(child);
    }

    /**
     * Recursive handle for insert case 5. Last step.
     * @param node Viewed node.
     */
    private void idInsertCase5(final MapNode node) {
        node.idParent.idColor = true;
        node.idGrandparent().idColor = false;
        if(node == node.idParent.idLeft
              && node.idParent == node.idGrandparent().idLeft) {
            rotateIdRight(node.idGrandparent());
        } else {
            assert node == node.idParent.idRight
              && node.idParent == node.idGrandparent().idRight;
            rotateIdLeft(node.idGrandparent());
        }
    }

    // **************************************************** //
    // ******* Mapping methods ******* //
    // ******************************* //

    /**
     * Modifies the mapping for the insertion of new nodes in the document.
     * @param id ID of the new node.
     * @param pre Pre-value of the new node.
     */
    public void insert(final int id, final int pre) {
      searchPreTree(pre, 1);

      if(current == null) {
        // insert as preRoot
        if(preRoot == null) {
          MapNode node = new MapNode();

          node.idmin = id;
          node.idmax = id;
          node.d     = pre - id;
          node.s     = 0;

          preRoot = node;
          insertIdTree(node);
        // insert as very last node
        } else {
          current = preRoot;

          // cycle to last node
          // sadd is still valid including that node
          while(current.preRight != null)
            current = current.preRight;

          // create new node
          MapNode node = new MapNode();
          node.idmin   = id;
          node.idmax   = id;
          node.d       = pre - sadd - id;
          node.s       = 0;

          insertPreTree(node);
          insertIdTree(node);
        }
      } else {
        MapNode split = new MapNode();
        split.idmin   = pre - current.d - sadd - current.s;
        split.idmax   = current.idmax;
        split.d       = current.d;
        split.s       = 1;

        // 1. case: current is completely affected
        if(pre == preMin()) {
            // split inherits addend of current
            split.s      += current.s;

            // current saves new node in preTree
            current.idmin = id;
            current.idmax = id;
            current.d     = pre - sadd - id;
            current.s     = 0;

            // replace current with split in idTree
            replaceNodeIdTree(current, split);
            split.idLeft  = current.idLeft;
            split.idRight = current.idRight;
            split.idColor = current.idColor;
            if(current.idLeft != null)
              current.idLeft.idParent = split;
            if(current.idRight != null)
              current.idRight.idParent = split;

            // insert current newly in idTree
            current.idParent = current.idLeft = current.idRight = null;
            current.idColor  = false;
            insertPreTree(split);

            // insert split in preTree
            insertIdTree(current);
        // 2. case: current is divided
        } else {
            MapNode node  = new MapNode();

            // split current
            current.idmax = split.idmin - 1;

            // create new mapnode for new node in document
            node.idmin    = id;
            node.idmax    = id;
            node.d        = pre - sadd - current.s - id;
            node.s        = 0;

            // insert split
            insertPreTree(split);
            insertIdTree(split);

            // insert new node
            insertPreTree(node);
            insertIdTree(node);
        }
       }
    }

    /**
     * Modfies the mapping if nodes from the document are deleted.
     * @param id ID of the new node.
     * @param pre Pre-value of the new node.
     */
    public void delete(final int id, final int pre) {
        searchPreTree(pre, -1);

        // 1. case: only one node => delete that node from preTree
        if(id == current.idmin && id == current.idmax) {
            // mark as deleted
            current.d  = Integer.MIN_VALUE;
            current.s -= 1;
            // delete current from preTree
            deletePreTree(current);
        // 2. case: pre-values of all nodes in current are changed
        } else if(id == current.idmin) {
            MapNode node = new MapNode();

            // change all values in MapNode
            current.idmin = id + 1;
            current.s    -= 1;

              // save deleted node
            node.idmin    = id;
            node.idmax    = id;
            node.d        = Integer.MIN_VALUE;
            insertIdTree(node);
        // 3. case: split current
        } else {
            MapNode split = new MapNode();
            MapNode node  = new MapNode();

            // set split-node
            split.idmin = id + 1;
            split.idmax = current.idmax;
            split.d     = current.d;
            split.s     = -1;

            // set current
            current.idmax = id - 1;

            // set the new node containing the deletes
            node.idmin = id;
            node.idmax = id;
            node.d     = Integer.MIN_VALUE;

            // insert split
            if(split.idmin <= split.idmax) {
              insertPreTree(split);
              insertIdTree(split);
            }

            // insert new node
            insertIdTree(node);
        }
    }

    /**
     * Gives the Pre-value of an ID.
     * @param id Calculate the Pre-Value for this ID.
     * @return The corresponding Pre-value.
     */
    public int pre(final int id) {
        MapNode child = searchIdTree(id);

        // check for invalid or deleted node
        if(child == null || child.d == Integer.MIN_VALUE)
            return Integer.MIN_VALUE;

        // initialize right value
        int pre = id + child.d + child.s;

        // cycle preTree to root
        while(child.preParent != null) {
            if(child.preParent.preRight == child)
                pre = pre + child.preParent.s;
            child = child.preParent;
        }

        return pre;
    }
}
