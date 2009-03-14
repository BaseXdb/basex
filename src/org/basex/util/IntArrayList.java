package org.basex.util;

import java.util.Iterator;
import org.basex.BaseX;

/**
 * This is a simple container for native int array values.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class IntArrayList implements Iterable<int[]> {
  /** Value array. */
  public int[][] list;
  /** Current array size. */
  public int size;
  
  /**
   * Default constructor.
   */
  public IntArrayList() {
    this(8);
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

  public Iterator<int[]> iterator() {
    return new Iterator<int[]>() {
      private int c = -1;
      public boolean hasNext() { return ++c < size; }
      public int[] next() { return list[c]; }
      public void remove() { BaseX.notimplemented(); }
    };
  }
}
