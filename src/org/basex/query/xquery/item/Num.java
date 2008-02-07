package org.basex.query.xquery.item;

/**
 * Abstract numeric item.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public abstract class Num extends Item {
  /**
   * Constructor.
   * @param t data type
   */
  protected Num(final Type t) {
    super(t);
  }
}
