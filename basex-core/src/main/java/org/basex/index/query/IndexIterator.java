package org.basex.index.query;

/**
 * Iterator for returning index results.
 *
 * @author BaseX Team 2005-20, BSD License
 * @author Christian Gruen
 */
public interface IndexIterator {
  /** Empty iterator. */
  IndexIterator EMPTY = new IndexIterator() {
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
  boolean more();

  /**
   * Returns the next pre value.
   * @return result
   */
  int pre();

  /**
   * Returns an approximate number of index results.
   * @return result approximate number of results
   */
  int size();
}
