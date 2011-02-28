package org.basex.util;

import java.util.Iterator;

/**
 * This is a simple container for int arrays.
 *
 * @author BaseX Team 2005-11, BSD License
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
   * @param e element to be set
   * @param i index
   */
  public void set(final int[] e, final int i) {
    if(i >= list.length) list = Array.copyOf(list, newSize(i + 1));
    list[i] = e;
    size = Math.max(size, i + 1);
  }

  @Override
  public Iterator<int[]> iterator() {
    return new Iterator<int[]>() {
      private int c = -1;
      @Override
      public boolean hasNext() { return ++c < size; }
      @Override
      public int[] next() { return list[c]; }
      @Override
      public void remove() { Util.notexpected(); }
    };
  }
}
