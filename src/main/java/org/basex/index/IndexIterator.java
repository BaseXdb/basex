package org.basex.index;

/**
 * This interface provides methods for returning index results.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public abstract class IndexIterator {
  /**
   * Empty iterator.
   */
  static final IndexIterator EMPTY = new IndexIterator() {
    @Override
    public boolean more() { return false; }
    @Override
    public int next() { return 0; }
    @Override
    public double score() { return -1; }
  };

  /**
   * Returns true if more results are found.
   * @return size
   */
  public abstract boolean more();

  /**
   * Returns the next result.
   * @return result
   */
  public abstract int next();

  /**
   * Returns the score value.
   * {@code -1} is returned if no scoring is available.
   * @return score value
   */
  public abstract double score();

  /**
   * Returns the number of index results. A new iterator must be created
   * after this method has been called.
   * @return result number of results
   */
  final int size() {
    int c = 0;
    while(more()) ++c;
    return c;
  }
}
