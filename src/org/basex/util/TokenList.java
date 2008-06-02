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
   * Adds several values via union.
   * @param val values to be added
   */
  public void union(final byte[][] val) {
    for(byte[] v : val) if(!contains(v)) add(v);
  }
  
  /**
   * Removes the specified values.
   * @param val values to be added
   */
  public void except(final byte[][] val) {
    for(byte[] v : val) {
      final int i = indexOf(v);
      if(i != -1) remove(i);
    }
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
   * Finds the position of the specified token.
   * @param v token to be checked
   * @return position
   */
  public int indexOf(final byte[] v) {
    for(int i = 0; i < size; i++) if(Token.eq(list[i], v)) return i;
    return -1;
  }
  
  /**
   * Removes the specified token.
   * @param i token to be removed
   */
  public void remove(final int i) {
    Array.move(list, i + 1, -1, size-- - i);
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

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("TokenList[");
    for(int i = 0; i < size; i++) {
      sb.append((i == 0 ? "" : ", ") + Token.string(list[i]));
    }
    sb.append("]");
    return sb.toString();
  }
}
