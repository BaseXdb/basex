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
  private byte[][] names;
  /** Value array. */
  private byte[][] values;

  /**
   * Default constructor.
   */
  public Atts() {
    this(1);
  }

  /**
   * Constructor with initial capacity.
   * @param capacity array capacity
   */
  public Atts(final long capacity) {
    final int c = Array.checkCapacity(capacity);
    names = new byte[c][];
    values = new byte[c][];
  }

  /**
   * Constructor.
   * @param atts  object to copy
   */
  public Atts(final Atts atts) {
    names = atts.names.clone();
    values = atts.values.clone();
    size = atts.size;
  }

  /**
   * Adds the next name/value pair.
   * @param name name to be added (can be {@code null})
   * @param value value to be added (can be {@code null})
   * @return self reference
   */
  public Atts add(final byte[] name, final byte[] value) {
    final int sz = size;
    if(sz == names.length) {
      final int s = Array.newCapacity(sz);
      names = Array.copyOf(names, s);
      values = Array.copyOf(values, s);
    }
    names[sz] = name;
    values[sz] = value;
    ++size;
    return this;
  }

  /**
   * Adds the next name/value pair.
   * @param name name to be added (can be {@code null})
   * @param value value to be added (can be {@code null})
   * @param stripNS strip namespaces
   * @return self reference
   */
  public Atts add(final byte[] name, final byte[] value, final boolean stripNS) {
    byte[] an = name;
    if(stripNS) {
      an = Token.local(an);
      int c = 0;
      while(contains(an)) an = Token.concat(Token.local(an), '_', Token.token(++c));
    }
    return add(an, value);
  }

  /**
   * Removes the element at the specified position.
   * @param index entry index
   * @return self reference
   */
  public Atts remove(final int index) {
    final int sz = size;
    Array.remove(names, index, 1, sz);
    Array.remove(values, index, 1, sz);
    names[sz - 1] = null;
    values[sz - 1] = null;
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
      if(Token.eq(names[i], name)) return i;
    }
    return -1;
  }

  /**
   * Returns the name at the specified index position.
   * @param index index
   * @return name
   */
  public byte[] name(final int index) {
    return names[index];
  }

  /**
   * Returns the value at the specified index position.
   * @param index index
   * @return value
   */
  public byte[] value(final int index) {
    return values[index];
  }

  /**
   * Returns the value for the specified name or {@code null}.
   * @param name name to be found
   * @return offset or -1
   */
  public byte[] value(final byte[] name) {
    final int i = get(name);
    return i == -1 ? null : values[i];
  }

  @Override
  public boolean equals(final Object obj) {
    if(obj == this) return true;
    if(!(obj instanceof Atts)) return false;
    final Atts a = (Atts) obj;
    if(size != a.size) return false;
    for(int i = 0; i < size; ++i) {
      if(!Token.eq(names[i], a.names[i]) || !Token.eq(values[i], a.values[i])) return false;
    }
    return true;
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder().add(Util.className(this)).add('[');
    for(int i = 0; i < size; ++i) {
      if(i > 0) tb.add(", ");
      tb.add(names[i]).add("=\"").add(values[i]).add("\"");
    }
    return tb.add("]").toString();
  }
}
