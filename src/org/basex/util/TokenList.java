package org.basex.util;

/**
 * This is a simple container for native byte[] values (tokens).
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class TokenList {
  /** Value array. */
  public byte[][] list = new byte[8][];
  /** Current array size. */
  public int size;
  
  /**
   * Adds next value.
   * @param v value to be added
   */
  public void add(final byte[] v) {
    if(size == list.length) list = Array.extend(list);
    list[size++] = v;
  }
  
  /**
   * Adds several values.
   * @param val values to be added
   */
  public void add(final byte[][] val) {
    for(byte[] v : val) add(v);
  }
  
  /**
   * Checks if the specified token is found in the list.
   * @param v token to be checked
   * @return true if value is found
   */
  public boolean contains(final byte[] v) {
    for(int i = 0; i < size; i++) if(Token.eq(list[i], v)) return true;
    return false;
  }

  /**
   * Finishes the int array.
   * @return int array
   */
  public byte[][] finish() {
    return size == list.length ? list : Array.finish(list, size);
  }

  /**
   * Resets the integer list.
   */
  public void reset() {
    size = 0;
  }
}
