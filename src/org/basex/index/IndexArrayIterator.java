package org.basex.index;

/**
 * This interface provides methods for returning index results.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public class IndexArrayIterator extends IndexIterator {
  /** Result array. */
  int[] result;
  /** Result array. */
  int[] result2;
  /** Counter. */
  private int d = -1;
  /** Second array flag. */
  private boolean first;
  
  /**
   * Constructor.
   * @param res result array
   */
  public IndexArrayIterator(final int[] res) {
    result = res;
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
    return ++d < result.length;
  }

  @Override
  public int next() {
    return (first ^= true) || result2 == null ? result[d] : result2[d];
  }
}
