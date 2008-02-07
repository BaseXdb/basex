package org.basex.util;

/**
 * This is a simple container for native boolean values.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class BoolList {
  /** Value array. */
  public boolean[] list = new boolean[8];
  /** Current array size. */
  public int size;

  /**
   * Default constructor.
   */
  public BoolList() { }

  /**
   * Adds next value.
   * @param v value to be added
   */
  public void add(final boolean v) {
    if(size == list.length) list = Array.extend(list);
    list[size++] = v;
  }

  /**
   * Finishes the int array.
   * @return int array
   */
  public boolean[] finish() {
    return size == list.length ? list : Array.finish(list, size);
  }

  /**
   * Resets the integer list.
   */
  public void reset() {
    size = 0;
  }
}
