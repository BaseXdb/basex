package org.basex.index;

/**
 * This interface provides methods for returning index results.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public abstract class IndexIterator {
  /** Empty iterator. */
  public static final IndexIterator EMPTY = new IndexIterator() {
    @Override
    public boolean more() { return false; }
    @Override
    public int next() { return 0; };
  };
  
  /**
   * Checks if more results can be expected.
   * @return result of check
   */
  public abstract boolean more();

  /**
   * Returns the next result.
   * @return result
   */
  public abstract int next();
}
