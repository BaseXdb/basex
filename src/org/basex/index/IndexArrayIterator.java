package org.basex.index;

/**
 * This interface provides methods for returning index results.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public class IndexArrayIterator extends IndexIterator {
  /** Result array. */
  private int[] result;
  /** Result array. */
  private int[] result2;
  /** Number of results. */
  private int size;
  /** Counter. */
  private int d = -1;
  /** Second array flag. */
  private boolean first;
  
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
    result = res;
    size = s;
  }
  
  /**
   * Constructor.
   * @param res result array
   * @param res2 second result array
   */
  public IndexArrayIterator(final int[] res, final int[] res2) {
    result = res;
    result2 = res2;
  }
  
  @Override
  public boolean more() {
    return ++d < size;
  }

  @Override
  public int next() {
    return (first ^= true) || result2 == null ? result[d] : result2[d];
  }

  @Override
  public int size() {
    return size;
  }
}
