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
  /** Flag for found values in list. **/
  public boolean found = false;
  
  /**
   * Adds next value.
   * @param v value to be added
   */
  public void add(final int[] v) {
    if(size == list.length) list = Array.extend(list);
    list[size++] = v;
    nb += v.length;
  }
  
  /**
   * Adds next value.
   * @param v value to be added
   * @param index int index where to add v
   */
  public void addAt(final int[] v, final int index) {
    if(size == list.length) list = Array.extend(list);
    System.arraycopy(list, index, list, index + 1, size - index);
    list[index] = v;
    size++;
    nb += v.length;
  }
  
  /**
   * Finishes the int array.
   * @return int array
   */
  public int[][] finish() {
    return size == list.length ? list : Array.finish(list, size);
  }
}
