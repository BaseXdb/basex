package org.basex.util;

import java.util.Arrays;
import java.util.Iterator;
import org.basex.core.Main;

/**
 * This is a simple container for native int array values.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class IntArrayList implements Iterable<int[]> {
  /** Value array. */
  int[][] list;
  /** Current array size. */
  int size;

  /**
   * Constructor.
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
    if(size == list.length) list = Arrays.copyOf(list, size << 1);
    list[size++] = v;
  }

  /**
   * Returns the number of entries.
   * @return number of entries
   */
  public int size() {
    return size;
  }

  /**
   * Returns the specified value.
   * @param p position
   * @return value
   */
  public int[] get(final int p) {
    return list[p];
  }

  /**
   * Sets the specified value at the specified position.
   * @param v value
   * @param p position
   */
  public void set(final int[] v, final int p) {
    list[p] = v;
  }

  public Iterator<int[]> iterator() {
    return new Iterator<int[]>() {
      private int c = -1;
      public boolean hasNext() { return ++c < size; }
      public int[] next() { return list[c]; }
      public void remove() { Main.notexpected(); }
    };
  }
}
