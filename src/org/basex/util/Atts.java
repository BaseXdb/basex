package org.basex.util;

/**
 * This is a simple container for attributes (keys and values).
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class Atts {
  /** Key array. */
  public byte[][] key = new byte[1][];
  /** Value array. */
  public byte[][] val = new byte[1][];
  /** Current array size. */
  public int size;

  /**
   * Sets the specified value.
   * @param k key to be added
   * @param v value to be added
   * @return self reference
   */
  public Atts set(final byte[] k, final byte[] v) {
    size = 0;
    add(k, v);
    return this;
  }

  /**
   * Adds next value.
   * @param k key to be added
   * @param v value to be added
   * @return self reference
   */
  public Atts add(final byte[] k, final byte[] v) {
    if(size == key.length) {
      key = Array.extend(key);
      val = Array.extend(val);
    }
    key[size] = k;
    val[size++] = v;
    return this;
  }

  /**
   * Deletes the specified entry.
   * @param i entry to be deleted
   */
  public void delete(final int i) {
    Array.move(key, i + 1, -1, --size - i);
    Array.move(val, i + 1, -1, size - i);
  }

  /**
   * Adds the specified values if the key is new.
   * @param k key to be checked
   * @param v value to be added
   * @return true if values were added
   */
  public boolean addUnique(final byte[] k, final byte[] v) {
    final boolean a = get(k) == -1;
    if(a) add(k, v);
    return a;
  }

  /**
   * Returns the reference for the specified key.
   * @param k key to be found
   * @return reference or -1
   */
  public int get(final byte[] k) {
    for(int i = size - 1; i >= 0; i--) if(Token.eq(key[i], k)) return i;
    return -1;
  }

  /**
   * Resets the integer list.
   * @return self reference
   */
  public Atts reset() {
    size = 0;
    return this;
  }
}
