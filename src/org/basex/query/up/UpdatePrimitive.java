package org.basex.query.up;

import org.basex.query.item.Nod;

/**
 * Abstract XQuery Update Primitive.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
public abstract class UpdatePrimitive {
  /** Target node of update expression. */
  Nod node;
  
  /**
   * Constructor.
   * @param n DBNode reference
   */
  public UpdatePrimitive(final Nod n) {
    node = n;
  }
}
