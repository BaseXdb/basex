package org.basex.index.query;

/**
 * Iterator for returning index results.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public abstract class IndexIterator {
  /** Empty iterator. */
  public static final IndexIterator EMPTY = new IndexIterator() {
    @Override
    public boolean more() { return false; }
    @Override
    public int pre() { return 0; }
    @Override
    public int size() { return 0; }
  };

  /**
   * Returns true if more results can be returned.
   * @return size
   */
  public abstract boolean more();

  /**
   * Returns the next pre value.
   * @return result
   */
  public abstract int pre();

  /**
   * Returns an approximate number of index results.
   * @return result approximate number of results
   */
  public abstract int size();
}
