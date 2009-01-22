package org.basex.query.iter;

/**
 * Iterator, which can be reset.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public abstract class ResetIter extends Iter {
  /**
   * Resets the iterator.
   */
  public abstract void reset();
}
