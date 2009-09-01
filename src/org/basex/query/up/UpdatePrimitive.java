package org.basex.query.up;

/**
 * Abstract XQuery Update Primitive.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public abstract class UpdatePrimitive {
  /** Node identity. */
  int id;
  /** Pre value of database node. */
  int pre;
  
  /**
   * Constructor.
   * @param nodeID node identity
   * @param nodePre node pre value
   */
  public UpdatePrimitive(final int nodeID, final int nodePre) {
    id = nodeID;
    pre = nodePre;
  }
}
