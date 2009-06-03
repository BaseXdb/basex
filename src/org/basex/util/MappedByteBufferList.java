package org.basex.util;

import java.nio.MappedByteBuffer;

/**
 * This is a simple List for MappedByteBuffer.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Xuan Moc
 */
public class MappedByteBufferList {
  /** Value array. */
  public MappedByteBuffer[] list;
  /** Number of entries. */
  public int size;

  /**
   * Default constructor.
   */
  public MappedByteBufferList() {
    this(8);
  }

  /**
   * Constructor, specifying an initial list size.
   * @param is initial size of the list
   */
  public MappedByteBufferList(final int is) {
    list = new MappedByteBuffer[is];
  }

  /**
   * Constructor, specifying an initial array.
   * @param v initial list values
   */
  public MappedByteBufferList(final MappedByteBuffer[] v) {
    list = v;
    size = v.length;
  }

  /**
   * Adds next value.
   * @param v value to be added
   */
  public void add(final MappedByteBuffer v) {
    if(size == list.length) list = Array.extend(list);
    list[size++] = v;
  }

  /**
   * Returns a value at the specified position.
   * @param p position
   * @return MappedByteBuffer
   */
  public MappedByteBuffer get(final int p) {
    return list[p];
  }
  
  /**
   * Sets a value at the specified position.
   * @param v value to be added
   * @param p position
   */
  public void set(final MappedByteBuffer v, final int p) {
    while(p >= list.length) list = Array.extend(list);
    list[p] = v;
    size = Math.max(size, p + 1);
  }

  /**
   * Checks if the specified value is found in the list.
   * @param v value to be added
   * @return true if value is found
   */
  public boolean contains(final MappedByteBuffer v) {
    for(int i = 0; i < size; i++) if(list[i] == v) return true;
    return false;
  }

  /**
   * Moves entries inside an array.
   * @param pos move position
   * @param off offset
   * @param l length
   */
  public void arraymove(final int pos, final int off, final int l) {
    Array.move(list, pos, off, l);
  }
  
  /**
   * Finishes the MappedByteBuffer array.
   * @return MappedByteBuffer array
   */
  public MappedByteBuffer[] finish() {
    return size == list.length ? list : Array.finish(list, size);
  }

  /**
   * Resets the list.
   */
  public void reset() {
    size = 0;
  }

}
