package org.basex.util;

import java.util.Arrays;

/**
 * This class is a very light container for storing bytes in an array.
 * To speedup storage, this class does not include any security checks
 * for array bounds, etc.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class Bytes {
  /** Bytes. */
  private byte[] bytes;
  /** Current position. */
  private int pos;

  /**
   * Constructor, specifying the array size.
   * @param i size
   */
  public Bytes(final int i) {
    bytes = new byte[i];
  }

  /**
   * Initializes the buffer to the specified size.
   * @param s size
   */
  public void init(final int s) {
    if(bytes.length != s) bytes = new byte[s];
    reset();
  }

  /**
   * Stores the specified byte and increases the pointer.
   * @param b byte to write
   */
  public void s(final byte b) {
    bytes[pos++] = b;
  }

  /**
   * Stores the specified byte and increases the pointer.
   * @param b byte to write
   */
  public void s(final int b) {
    s((byte) b);
  }

  /**
   * Stores the specified byte and increases the pointer.
   * @param b byte to write
   */
  public void s(final long b) {
    s((byte) b);
  }

  /**
   * Returns the byte array.
   * @return byte array
   */
  public byte[] get() {
    return pos == bytes.length ? bytes : Arrays.copyOf(bytes, pos);
  }

  /**
   * Returns the current pointer.
   * @return pointer
   */
  public int pos() {
    return pos;
  }

  /**
   * Resets the pointer.
   */
  public void reset() {
    pos = 0;
  }
}
