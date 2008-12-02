package org.basex.index;

/**
 * FTNode Iterator interface.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 * @author Sebastian Gath
 */

public abstract class FTNodeIter {
  /** Empty node iterator. */
  public static final FTNodeIter EMPTY = new FTNodeIter() {
    @Override
    public FTNode next() { return null; }
    @Override
    public String toString() { return "()"; }
  };

  /**
   * Returns next FTNode or null.
   * @return next FTNode
   */
  public abstract FTNode next();

}
