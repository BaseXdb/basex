package org.basex.util.list;

import java.util.Iterator;

import org.basex.util.Array;
import org.basex.util.Util;

/**
 * This is a simple container for native integer arrays.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class IntArrayList extends ElementList implements Iterable<int[]> {
  /** Elements container. */
  int[][] list;

  /**
   * Default constructor.
   */
  public IntArrayList() {
    this(CAP);
  }

  /**
   * Constructor, specifying an initial array capacity.
   * @param c initial capacity
   */
  public IntArrayList(final int c) {
    list = new int[c][];
  }

  /**
   * Adds an element.
   * @param e element to be added
   */
  public void add(final int[] e) {
    if(size == list.length) list = Array.copyOf(list, newSize());
    list[size++] = e;
  }

  /**
   * Returns the element at the specified index.
   * @param i index
   * @return element
   */
  public int[] get(final int i) {
    return list[i];
  }

  /**
   * Sets an element at the specified index.
   * @param i index
   * @param e element to be set
   */
  public void set(final int i, final int[] e) {
    if(i >= list.length) list = Array.copyOf(list, newSize(i + 1));
    list[i] = e;
    size = Math.max(size, i + 1);
  }

  @Override
  public Iterator<int[]> iterator() {
    return new Iterator<int[]>() {
      private int c;
      @Override
      public boolean hasNext() { return c < size; }
      @Override
      public int[] next() { return list[c++]; }
      @Override
      public void remove() { Util.notexpected(); }
    };
  }
}
