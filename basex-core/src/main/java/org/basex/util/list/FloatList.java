package org.basex.util.list;

import java.util.*;

import org.basex.util.*;

/**
 * Resizable-array implementation for native float values.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FloatList extends ElementList {
  /** Element container. */
  private float[] list;

  /**
   * Default constructor.
   */
  public FloatList() {
    this(-1);
  }

  /**
   * Constructor with initial capacity.
   * @param capacity array capacity
   */
  public FloatList(final long capacity) {
    list = new float[Array.initialCapacity(capacity)];
  }

  /**
   * Adds an element to the array.
   * @param element element to be added
   * @return self reference
   */
  public FloatList add(final float element) {
    float[] lst = list;
    final int s = size;
    if(s == lst.length) {
      lst = Arrays.copyOf(lst, newCapacity());
      list = lst;
    }
    lst[s] = element;
    size = s + 1;
    return this;
  }

  /**
   * Adds elements to the array.
   * @param elements elements to be added
   * @return self reference
   */
  public FloatList add(final float... elements) {
    float[] lst = list;
    final int l = elements.length, s = size, ns = s + l;
    if(ns > lst.length) {
      lst = Arrays.copyOf(lst, newCapacity(ns));
      list = lst;
    }
    Array.copyFromStart(elements, l, lst, s);
    size = ns;
    return this;
  }

  /**
   * Returns the element at the specified position.
   * @param index index of the element to return
   * @return element
   */
  public float get(final int index) {
    return list[index];
  }

  /**
   * Returns an array with all elements.
   * @return array
   */
  public float[] toArray() {
    return Arrays.copyOf(list, size);
  }

  /**
   * Returns an array with all elements and invalidates the internal array.
   * Warning: the function must only be called if the list is discarded afterward.
   * @return array (internal representation!)
   */
  public float[] finish() {
    final float[] lst = list;
    list = null;
    final int s = size;
    return s == lst.length ? lst : Arrays.copyOf(lst, s);
  }

  @Override
  public boolean equals(final Object obj) {
    return obj == this || obj instanceof final FloatList l &&
        Arrays.equals(list, 0, size, l.list, 0, l.size);
  }

  @Override
  public String toString() {
    return list == null ? "" : Arrays.toString(toArray());
  }
}
