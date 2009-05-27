package org.basex.index;

/**
 * This interface provides methods for returning index results.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public interface IndexIterator {
  /**
   * Empty iterator.
   */
  IndexIterator EMPTY = new IndexIterator() {
    public boolean more() { return false; };
    public int next() { return 0; };
  };
  
  /**
   * Returns true if more results are found.
   * @return size
   */
  boolean more();

  /**
   * Returns the next result.
   * @return result
   */
  int next();
}
