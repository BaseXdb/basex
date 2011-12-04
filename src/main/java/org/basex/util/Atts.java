package org.basex.util;

/**
 * This is a simple container for attributes (name/string pairs).
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class Atts {
  /** Name array. */
  private byte[][] nm = new byte[1][];
  /** String array. */
  private byte[][] st = new byte[1][];
  /** Current array size. */
  private int size;

  /**
   * Adds the next name/string pair.
   * @param name name to be added
   * @param string string to be added
   * @return self reference
   */
  public Atts add(final byte[] name, final byte[] string) {
    if(size == nm.length) {
      final int s = size << 1;
      nm = Array.copyOf(nm, s);
      st = Array.copyOf(st, s);
    }
    nm[size] = name;
    st[size++] = string;
    return this;
  }

  /**
   * Deletes the specified entry.
   * @param i entry offset
   */
  public void delete(final int i) {
    Array.move(nm, i + 1, -1, --size - i);
    Array.move(st, i + 1, -1, size - i);
  }

  /**
   * Checks if the specified name is found.
   * @param name name to be found
   * @return result of check
   */
  public boolean contains(final byte[] name) {
    return get(name) != -1;
  }

  /**
   * Returns the offset to the specified name.
   * @param name name to be found
   * @return offset or -1
   */
  public int get(final byte[] name) {
    for(int i = 0; i < size; ++i) if(Token.eq(nm[i], name)) return i;
    return -1;
  }

  /**
   * Returns the name at the specified index position.
   * @param i index
   * @return name
   */
  public byte[] name(final int i) {
    return nm[i];
  }

  /**
   * Returns the string at the specified index position.
   * @param i index
   * @return string
   */
  public byte[] string(final int i) {
    return st[i];
  }

  /**
   * Returns the string for the specified name, or {@code null}.
   * @param name name to be found
   * @return offset or -1
   */
  public byte[] string(final byte[] name) {
    final int i = get(name);
    return i == -1 ? null : st[i];
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
      tb.add(nm[i]).add("=\"").add(st[i]).add("\"");
    }
    return tb.add("]").toString();
  }
}
