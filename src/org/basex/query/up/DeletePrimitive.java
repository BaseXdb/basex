package org.basex.query.up;

/**
 * Represents a delete primitive.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public class DeletePrimitive extends UpdatePrimitive {

  /**
   * Constructor.
   * @param nodeID node identity
   * @param nodePre node pre value
   */
  public DeletePrimitive(int nodeID, int nodePre) {
    super(nodeID, nodePre);
  }
}
