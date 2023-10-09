package org.basex.util;

import static org.basex.util.Token.*;
import org.basex.util.list.*;

/**
 * Resizable-array implementation for attributes (name/value pairs).
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class Atts extends ElementList {
  /** Name/value pairs. */
  private byte[][] list;

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
    list = new byte[Array.checkCapacity(capacity << 1)][];
  }

  /**
   * Constructor.
   * @param atts object to copy
   */
  public Atts(final Atts atts) {
    list = atts.list.clone();
    size = atts.size;
  }

  @Override
  public int size() {
    return size >>> 1;
  }

  @Override
  public void size(final int sz) {
    size = sz << 1;
  }

  /**
   * Adds the next name/value pair.
   * @param name name to be added (can be {@code null})
   * @param value value to be added (can be {@code null})
   * @return self reference
   */
  public Atts add(final byte[] name, final byte[] value) {
    byte[][] lst = list;
    final int s = size;
    if(s == lst.length) {
      lst = Array.copyOf(lst, Array.newCapacity(s >>> 1) << 1);
      list = lst;
    }
    lst[s] = name;
    lst[s + 1] = value;
    size = s + 2;
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
    if(!stripNS) return add(name, value);

    final byte[] local = local(name);
    byte[] an = local;
    int c = 0;
    while(contains(an)) an = concat(local, '_', token(++c));
    return add(an, value);
  }

  /**
   * Removes the element at the specified position.
   * @param index entry index
   * @return self reference
   */
  public Atts remove(final int index) {
    final byte[][] lst = list;
    final int s = size;
    Array.remove(lst, index, 2, s);
    lst[s - 2] = null;
    lst[s - 1] = null;
    size = s - 2;
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
    final byte[][] lst = list;
    for(int p = 0; p < size; p += 2) {
      if(eq(lst[p], name)) return p >>> 1;
    }
    return -1;
  }

  /**
   * Returns the name at the specified index position.
   * @param index index
   * @return name
   */
  public byte[] name(final int index) {
    return list[index << 1];
  }

  /**
   * Returns the value at the specified index position.
   * @param index index
   * @return value
   */
  public byte[] value(final int index) {
    return list[(index << 1) + 1];
  }

  /**
   * Returns the value for the specified name or {@code null}.
   * @param name name to be found
   * @return offset or -1
   */
  public byte[] value(final byte[] name) {
    final int i = get(name);
    return i == -1 ? null : value(i);
  }

  /**
   * Optimizes the attribute list.
   * @return self reference
   */
  public Atts optimize() {
    final byte[][] lst = list;
    if(size != lst.length) list = Array.copyOf(lst, size);
    return this;
  }

  @Override
  public boolean equals(final Object obj) {
    return obj == this || obj instanceof Atts && equals((Atts) obj);
  }

  /**
   * Compares attributes.
   * @param atts attributes to be compared
   * @return result of check
   */
  public boolean equals(final Atts atts) {
    final int s1 = size, s2 = atts.size;
    if(s1 != s2) return false;
    final byte[][] lst1 = list, lst2 = atts.list;

    for(int p1 = 0; p1 < s1; p1 += 2) {
      for(int p2 = 0;; p2 += 2) {
        if(p2 == s1) return false;
        if(eq(lst1[p1], lst2[p2])) {
          if(eq(lst1[p1 + 1], lst2[p2 + 1])) break;
          return false;
        }
      }
    }
    return true;
  }

  @Override
  public String toString() {
    final TokenBuilder tb = new TokenBuilder().add(Util.className(this)).add('[');
    for(int p = 0; p < size; p += 2) {
      if(p > 0) tb.add(", ");
      tb.add(list[p]).add("=\"").add(list[p + 1]).add("\"");
    }
    return tb.add("]").toString();
  }
}
