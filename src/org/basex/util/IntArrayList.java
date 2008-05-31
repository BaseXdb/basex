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
  
  /**
   * Default constructor.
   */
  public IntArrayList() {
  }
  
  /**
   * Constructor.
   * 
   * @param is initial size of the list
   */
  public IntArrayList(final int is) {
    list = new int[is][];
  }
  
  /**
   * Adds next value.
   * @param v value to be added
   */
  public void add(final int[] v) {
    if(size == list.length) list = Array.extend(list);
    list[size++] = v;
  }
  
  /**
   * Finishes the int array.
   * @return int array
   */
  public int[][] finish() {
    return size == list.length ? list : Array.finish(list, size);
  }
}
