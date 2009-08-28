package org.basex.query.up;


/**
 * Represents a rename primitive.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public class RenamePrimitive extends UpdatePrimitive {
  /** New name. */
  byte[] newName;
  
  /**
   * Constructor.
   * @param nodeID node identity
   * @param nodePre node pre value
   * @param n new name
   */
  public RenamePrimitive(final int nodeID, final int nodePre, final byte[] n) {
    super(nodeID, nodePre);
    newName = n;
  }
}
