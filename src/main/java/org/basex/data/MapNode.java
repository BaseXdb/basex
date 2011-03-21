package org.basex.data;

/**
 * This class gives the nodes for the MapTree.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Philipp Ziemer
 */
class MapNode {
  /** Minimal ID in the node. */
  public int idmin;
  /** Maximal ID in the node. */
  public int idmax;
  /** Distance from the IDs to the Min-Max-interval. */
  public int d;
  /** Addend valid for the node and right sub-PreTree. */
  public int s;
  /** Parent-node in the preTree. */
  public MapNode preParent;
  /** Left child-node in the preTree. */
  public MapNode preLeft;
  /** Right child-node in the preTree. */
  public MapNode preRight;
  /** Node-color in the preTree (false is read, true is black). */
  public boolean preColor;
  /** Parent-node in the idTree. */
  public MapNode idParent;
  /** Left child-node in the idTree. */
  public MapNode idLeft;
  /** Right child-node in the idTree. */
  public MapNode idRight;
  /** Node-color in the idTree (false is read, true is black). */
  public boolean idColor;

  /**
   * Calculates the grandparent of the map-node in the preTree.
   * @return Grandparent if there is one, else null.
   */
  public MapNode preGrandparent() {
    assert preParent != null; // Not the root node
    assert preParent.preParent != null; // Not child of root
    return preParent.preParent;
  }

  /**
   * Calculates the sibling of the map-node in the preTree.
   * @return Sibling if there is one, else null.
   */
  public MapNode preSibling() {
    assert preParent != null; // Root node has no sibling
    if(this == preParent.preLeft) return preParent.preRight;

    return preParent.preLeft;
  }

  /**
   * Calculates the uncle of the map-node in the preTree.
   * @return Uncle if there is one, else null.
   */
  public MapNode preUncle() {
    assert preParent != null; // Root node has no uncle
    assert preParent.preParent != null; // Children of root have no uncle
    return preParent.preSibling();
  }

  /**
   * Calculates the grandparent of the map-node in the idTree.
   * @return Grandparent if there is one, else null.
   */
  public MapNode idGrandparent() {
    assert idParent != null; // Not the root node
    assert idParent.idParent != null; // Not child of root
    return idParent.idParent;
  }

  /**
   * Calculates the sibling of the map-node in the idTree.
   * @return Sibling if there is one, else null.
   */
  public MapNode idSibling() {
    assert idParent != null; // Root node has no sibling
    if(this == idParent.idLeft) return idParent.idRight;

    return idParent.idLeft;
  }

  /**
   * Calculates the uncle of the map-node in the idTree.
   * @return Uncle if there is one, else null.
   */
  public MapNode idUncle() {
    assert idParent != null; // Root node has no uncle
    assert idParent.idParent != null; // Children of root have no uncle
    return idParent.idSibling();
  }
}
