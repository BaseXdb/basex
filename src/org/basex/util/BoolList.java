package org.basex.util;

/**
 * This is a simple container for native boolean values.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class BoolList {
  /** Value array. */
  public boolean[] list;
  /** Current array size. */
  public int size;

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
   * Checks if all values are b.
   * @param b boolean b
   * @return boolean all b
   */
  public boolean all(final boolean b) {
    for(final boolean bl : list) if(bl != b) return false;
    return true;
  }

  /**
   * Finishes the int array.
   * @return int array
   */
  public boolean[] finish() {
    return size == list.length ? list : Array.finish(list, size);
  }
}
