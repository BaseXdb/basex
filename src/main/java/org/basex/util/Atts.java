package org.basex.util;

/**
 * This is a simple container for attributes (name/value pairs).
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Atts {
  /** Name array. */
  private byte[][] nm = new byte[1][];
  /** Value array. */
  private byte[][] vl = new byte[1][];
  /** Current array size. */
  private int size;

  /**
   * Default constructor.
   */
  public Atts() {
    nm = new byte[1][];
    vl = new byte[1][];
  }

  /**
   * Constructor, specifying an initial entry.
   * @param name name to be added
   * @param value value to be added
   */
  public Atts(final byte[] name, final byte[] value) {
    nm = new byte[][] { name };
    vl = new byte[][] { value };
    size = 1;
  }

  /**
   * Adds the next name/value pair.
   * @param name name to be added
   * @param value value to be added
   * @return self reference
   */
  public Atts add(final byte[] name, final byte[] value) {
    if(size == nm.length) {
      final int s = size << 1;
      nm = Array.copyOf(nm, s);
      vl = Array.copyOf(vl, s);
    }
    nm[size] = name;
    vl[size++] = value;
    return this;
  }

  /**
   * Deletes the specified entry.
   * @param i entry offset
   */
  public void delete(final int i) {
    Array.move(nm, i + 1, -1, --size - i);
    Array.move(vl, i + 1, -1, size - i);
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
   * Returns the value at the specified index position.
   * @param i index
   * @return value
   */
  public byte[] value(final int i) {
    return vl[i];
  }

  /**
   * Returns the value for the specified name, or {@code null}.
   * @param name name to be found
   * @return offset or -1
   */
  public byte[] value(final byte[] name) {
    final int i = get(name);
    return i == -1 ? null : vl[i];
  }

  /**
   * Returns the number of attributes.
   * @return number of attributes
   */
  public int size() {
    return size;
  }

  /**
   * Enforces the number of attributes.
   * @param s number of attributes
   */
  public void size(final int s) {
    size = s;
  }

  /**
   * Clears the container.
   * @return self reference
   */
  public Atts clear() {
    size = 0;
    return this;
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder(Util.name(this) + '[');
    for(int i = 0; i < size; ++i) {
      if(i > 0) tb.add(", ");
      tb.add(nm[i]).add("=\"").add(vl[i]).add("\"");
    }
    return tb.add("]").toString();
  }

  /**
   * Creates a shallow copy which shares all keys and values.
   * @return shallow copy
   */
  public Atts copy() {
    final Atts copy = new Atts();
    copy.nm = nm.clone();
    copy.vl = vl.clone();
    copy.size = size;
    return copy;
  }
}
