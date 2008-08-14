package org.basex.util;

/**
 * This is a simple container for native byte array values.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class ByteArrayList {
  /** Value array. */
  public byte[][] list = new byte[4][];
  /** Current array size. */
  public int size;
  /** Counter. */
  private int c = -1;
  
  /**
   * Default constructor.
   */
  public ByteArrayList() {
  }
  
  /**
   * Constructor.
   * 
   * @param is initial size of the list
   */
  public ByteArrayList(final int is) {
    list = new byte[is][];
  }
  
  /**
   * Adds next value.
   * @param v value to be added
   */
  public void add(final byte[] v) {
    if(size == list.length) list = Array.extend(list);
    list[size++] = v;
  }
  
  /**
   * Finishes the byte array.
   * @return byte array
   */
  public byte[][] finish() {
    return size == list.length ? list : Array.finish(list, size);
  }
  
  /**
   * Checks for more byte[].
   * 
   * @return boolean more
   */
  public boolean more() {
    return ++c < size;
  }
  /**
   * Get next byte[].
   * @return next byte[]
   */
  public byte[] next() {
    return (c < size) ? list[c] : null;
  }
}
