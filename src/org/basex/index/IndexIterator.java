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
    public int next() { return 0; };
    public int size() { return 0; }; 
  };

  /**
   * Returns the next result.
   * @return result
   */
  int next();
  
  /**
   * Returns the number of index results.
   * @return size
   */
  int size();
}
