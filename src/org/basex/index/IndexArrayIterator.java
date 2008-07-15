package org.basex.index;

/**
 * This interface provides methods for returning index results.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public class IndexArrayIterator extends IndexIterator {
  /** Result array. */
  private int[][] result;
  /** Number of results. */
  private int size;
  /** Counter. */
  private int d = -1;
  /** Array counter. */
  private int c = 0;
  
  /**
   * Constructor.
   * @param res result array
   */
  public IndexArrayIterator(final int[] res) {
    this(res, res.length);
  }
  
  /**
   * Constructor.
   * @param res result array
   * @param s number of results
   */
  public IndexArrayIterator(final int[] res, final int s) {
    this(new int[][] { res }, s);
  }
  
  /**
   * Constructor.
   * @param res result array
   */
  public IndexArrayIterator(final int[][] res) {
    this(res, res.length);
  }
  
  /**
   * Constructor.
   * @param res result array
   * @param s number of results
   */
  public IndexArrayIterator(final int[][] res, final int s) {
    result = res;
    size = s;
  }
  
  @Override
  public int next() {
    if(++d == size) { c++; d = 0; }
    return result[c][d];
  }

  @Override
  public int size() {
    return size;
  }
}
