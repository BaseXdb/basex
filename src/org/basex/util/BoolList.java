package org.basex.util;

/**
 * This is a simple container for native boolean values.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class BoolList {
  /** Value array. */
  private boolean[] list;
  /** Current array size. */
  private int size;

  /**
   * Default constructor.
   */
  public BoolList() {
    this(8);
  }

  /**
   * Constructor, specifying the initial array size.
   * @param c initial size
   */
  public BoolList(final int c) {
    list = new boolean[c];
  }

  /**
   * Adds next value.
   * @param v value to be added
   */
  public void add(final boolean v) {
    if(size == list.length) list = Array.extend(list);
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
  public boolean get(final int p) {
    return list[p];
  }

  /**
   * Sets a value at the specified position.
   * @param v value to be added
   * @param p position
   */
  public void set(final boolean v, final int p) {
    while(p >= list.length) list = Array.extend(list);
    list[p] = v;
    size = Math.max(size, p + 1);
  }

  /**
   * Finishes the int array.
   * @return int array
   */
  public boolean[] finish() {
    return size == list.length ? list : Array.finish(list, size);
  }
}
