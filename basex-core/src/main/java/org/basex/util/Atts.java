package org.basex.util;

import org.basex.util.list.*;

/**
 * Resizable-array implementation for attributes (name/value pairs).
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class Atts extends ElementList {
  /** Name array. */
  private byte[][] nm;
  /** Value array. */
  private byte[][] vl;

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
   * @param name name to be added (can be {@code null})
   * @param value value to be added (can be {@code null})
   * @return self reference
   */
  public Atts add(final byte[] name, final byte[] value) {
    final int sz = size;
    if(sz == nm.length) {
      final int s = Array.newCapacity(sz);
      nm = Array.copyOf(nm, s);
      vl = Array.copyOf(vl, s);
    }
    nm[sz] = name;
    vl[sz] = value;
    ++size;
    return this;
  }

  /**
   * Removes the element at the specified position.
   * @param index entry index
   * @return self reference
   */
  public Atts remove(final int index) {
    final int sz = size;
    Array.remove(nm, index, 1, sz);
    Array.remove(vl, index, 1, sz);
    nm[sz - 1] = null;
    vl[sz - 1] = null;
    --size;
    return this;
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
    for(int i = 0; i < size; ++i) {
      if(Token.eq(nm[i], name)) return i;
    }
    return -1;
  }

  /**
   * Returns the name at the specified index position.
   * @param index index
   * @return name
   */
  public byte[] name(final int index) {
    return nm[index];
  }

  /**
   * Returns the value at the specified index position.
   * @param index index
   * @return value
   */
  public byte[] value(final int index) {
    return vl[index];
  }

  /**
   * Returns the value for the specified name or {@code null}.
   * @param name name to be found
   * @return offset or -1
   */
  public byte[] value(final byte[] name) {
    final int i = get(name);
    return i == -1 ? null : vl[i];
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

  @Override
  public boolean equals(final Object obj) {
    if(obj == this) return true;
    if(!(obj instanceof Atts)) return false;
    final Atts a = (Atts) obj;
    if(size != a.size) return false;
    for(int i = 0; i < size; ++i) {
      if(!Token.eq(nm[i], a.nm[i]) || !Token.eq(vl[i], a.vl[i])) return false;
    }
    return true;
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder().add(Util.className(this)).add('[');
    for(int i = 0; i < size; ++i) {
      if(i > 0) tb.add(", ");
      tb.add(nm[i]).add("=\"").add(vl[i]).add("\"");
    }
    return tb.add("]").toString();
  }
}
