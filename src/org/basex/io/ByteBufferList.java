package org.basex.io;

import java.nio.MappedByteBuffer;

import org.basex.util.Array;

/**
 * This is a simple list for ByteBuffer.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Xuan Moc
 */
class ByteBufferList {
  /** Value array. */
  private MappedByteBuffer[] list;
  /** Number of entries. */
  private int size;

  /**
   * Constructor, specifying an initial list size.
   * @param is initial size of the list
   */
  ByteBufferList(final int is) {
    list = new MappedByteBuffer[is];
  }

  /**
   * Adds next value.
   * @param v value to be added
   */
  void add(final MappedByteBuffer v) {
    if(size == list.length) list = Array.extend(list);
    list[size++] = v;
  }

  /**
   * Returns a value at the specified position.
   * @param p position
   * @return MappedByteBuffer
   */
  MappedByteBuffer get(final int p) {
    return list[p];
  }

  /**
   * Sets a value at the specified position.
   * @param v value to be added
   * @param p position
   */
  void set(final MappedByteBuffer v, final int p) {
    list[p] = v;
    size = Math.max(size, p + 1);
  }
}
