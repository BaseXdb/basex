package org.basex.query.up;

import org.basex.query.item.Nod;

/**
 * Abstract XQuery Update Primitive.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Lukas Kircher
 */
abstract class UpdatePrimitive {
  /** Target node of update expression. */
  final Nod node;
  
  /**
   * Constructor.
   * @param n DBNode reference
   */
  protected UpdatePrimitive(final Nod n) {
    node = n;
  }
  
  /**
   * Applies the update operation represented by this primitive to the 
   * database.s 
   */
  public abstract void apply();
}
