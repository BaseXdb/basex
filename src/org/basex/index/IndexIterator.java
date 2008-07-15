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
    public int next() { return 0; };
    @Override
    public int size() { return 0; };
  };

  /**
   * Returns the next result.
   * @return result
   */
  public abstract int next();
  
  /**
   * Returns the number of index results.
   * @return size
   */
  public abstract int size();
}
