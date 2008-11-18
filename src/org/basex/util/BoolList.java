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
  
  /**
   * Checks if all values are b.
   * @param b boolean b
   * @return boolean all b
   */
  public boolean all(final boolean b) {
    for (boolean bl : list) if(!bl && b) return false;
    return true;
  }
  
}
