package org.basex.util.list;

import java.util.*;

import org.basex.util.*;

/**
 * Resizable-array implementation for native shorts.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public class ShortList extends ElementList {
  /** Element container. */
  protected short[] list;

  /**
   * Default constructor.
   */
  public ShortList() {
    this(Array.INITIAL_CAPACITY);
  }

  /**
   * Constructor with initial array capacity.
   * @param capacity array capacity
3   */
  public ShortList(final long capacity) {
    list = new short[Array.checkCapacity(capacity)];
  }

  /**
   * Adds an element to the array.
   * @param element element to be added
   * @return self reference
   */
  public ShortList add(final short element) {
    short[] lst = list;
    final int s = size;
    if(s == lst.length) lst = Arrays.copyOf(lst, newCapacity());
    lst[s] = element;
    list = lst;
    size = s + 1;
    return this;
  }

  /**
   * Adds elements to the array.
   * @param elements elements to be added
   * @return self reference
   */
  public final ShortList add(final short... elements) {
    short[] lst = list;
    final int l = elements.length, s = size, ns = s + l;
    if(ns > lst.length) lst = Arrays.copyOf(lst, newCapacity(ns));
    Array.copyFromStart(elements, l, lst, s);
    list = lst;
    size = ns;
    return this;
  }

  /**
   * Returns an array with all elements.
   * @return array
   */
  public short[] toArray() {
    return Arrays.copyOf(list, size);
  }

  /**
   * Returns an array with all elements and invalidates the internal array.
   * Warning: the function must only be called if the list is discarded afterwards.
   * @return array (internal representation!)
   */
  public short[] finish() {
    final short[] lst = list;
    list = null;
    final int s = size;
    return s == lst.length ? lst : Arrays.copyOf(lst, s);
  }

  @Override
  public boolean equals(final Object obj) {
    if(obj == this) return true;
    if(!(obj instanceof ShortList)) return false;
    final ShortList bl = (ShortList) obj;
    if(size != bl.size) return false;
    for(int l = 0; l < size; ++l) {
      if(list[l] != bl.list[l]) return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return list == null ? "" : Arrays.toString(toArray());
  }
}
