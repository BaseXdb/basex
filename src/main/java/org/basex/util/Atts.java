package org.basex.util;

/**
 * This is a simple container for attributes (key/value tokens).
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class Atts {
  /** Key array. */
  private byte[][] key = new byte[1][];
  /** Value array. */
  private byte[][] val = new byte[1][];
  /** Current array size. */
  private int size;

  /**
   * Adds the next key/value pair.
   * @param k key to be added
   * @param v value to be added
   * @return self reference
   */
  public Atts add(final byte[] k, final byte[] v) {
    if(size == key.length) {
      final int s = size << 1;
      key = Array.copyOf(key, s);
      val = Array.copyOf(val, s);
    }
    key[size] = k;
    val[size++] = v;
    return this;
  }

  /**
   * Deletes the specified entry.
   * @param i entry offset
   */
  public void delete(final int i) {
    Array.move(key, i + 1, -1, --size - i);
    Array.move(val, i + 1, -1, size - i);
  }

  /**
   * Checks if the specified key is found.
   * @param k key to be checked
   * @return result of check
   */
  public boolean contains(final byte[] k) {
    return get(k) != -1;
  }

  /**
   * Returns the offset to the specified key.
   * @param k key to be found
   * @return offset or -1
   */
  public int get(final byte[] k) {
    for(int i = 0; i < size; ++i) if(Token.eq(key[i], k)) return i;
    return -1;
  }

  /**
   * Returns the key at the specified index position.
   * @param i index
   * @return key
   */
  public byte[] key(final int i) {
    return key[i];
  }

  /**
   * Returns the value at the specified index position.
   * @param i index
   * @return key
   */
  public byte[] val(final int i) {
    return val[i];
  }

  /**
   * Returns the number of attributes.
   * @return number of attributes
   */
  public int size() {
    return size;
  }

  /**
   * Sets the number of attributes.
   * @param s number of attributes
   */
  public void size(final int s) {
    size = s;
  }

  /**
   * Resets the container.
   * @return self reference
   */
  public Atts reset() {
    size = 0;
    return this;
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder(Util.name(this) + "[");
    for(int i = 0; i < size; ++i) {
      if(i > 0) tb.add(", ");
      tb.add(key[i]).add("=\"").add(val[i]).add("\"");
    }
    return tb.add("]").toString();
  }
}
