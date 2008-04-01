package org.basex.util;

/**
 * This is a simple container for native int array values.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class IntArrayList {
  /** Value array. */
  public int[][] list = new int[8][];
  /** Current array size. */
  public int size;
  /** Current used buckets - SPECIAL COUNT, NOT GENERAL. **/
  public int nb = 0;
  /** Used for special count of nb. **/
  private boolean sc = false;
  /**
   * Default constructor.
   */
  public IntArrayList() { }

  /**
   * Constructor with use of special count.
   * @param specialCount enable/disable special count.
   */
  public IntArrayList(final boolean specialCount) { 
    sc = specialCount;
  }

  
  /**
   * Adds next value.
   * @param v value to be added
   */
  public void add(final int[] v) {
    if(size == list.length) list = Array.extend(list);
    list[size++] = v;
    if(sc) nb += v[0] + v[v[0] + 2] * 8 + 4;
      else nb += v.length;
  }

  /**
   * Finishes the int array.
   * @return int array
   */
  public int[][] finish() {
    return size == list.length ? list : Array.finish(list, size);
  }

  /**
   * Resets the integer list.
   */
  public void reset() {
    size = 0;
    nb = 0;
  }
}
