package org.basex.util.list;

import java.util.*;

import org.basex.util.*;

/**
 * Resizable-array implementation for native double values.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public class DoubleList extends ElementList {
  /** Element container. */
  protected double[] list;

  /**
   * Default constructor.
   */
  public DoubleList() {
    this(Array.INITIAL_CAPACITY);
  }

  /**
   * Constructor with initial capacity.
   * @param capacity array capacity
   */
  public DoubleList(final long capacity) {
    list = new double[Array.checkCapacity(capacity)];
  }

  /**
   * Adds an element to the array.
   * @param element element to be added
   * @return self reference
   */
  public final DoubleList add(final double element) {
    double[] lst = list;
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
  public final DoubleList add(final double... elements) {
    double[] lst = list;
    final int l = elements.length, s = size, ns = s + l;
    if(ns > lst.length) lst = Arrays.copyOf(lst, newCapacity(ns));
    Array.copyFromStart(elements, l, lst, s);
    list = lst;
    size = ns;
    return this;
  }

  /**
   * Returns the element at the specified position.
   * @param index index of the element to return
   * @return element
   */
  public final double get(final int index) {
    return list[index];
  }

  /**
   * Returns an array with all elements.
   * @return array
   */
  public final double[] toArray() {
    return Arrays.copyOf(list, size);
  }

  /**
   * Returns an array with all elements and invalidates the internal array.
   * Warning: the function must only be called if the list is discarded afterwards.
   * @return array (internal representation!)
   */
  public double[] finish() {
    final double[] lst = list;
    list = null;
    final int s = size;
    return s == lst.length ? lst : Arrays.copyOf(lst, s);
  }

  @Override
  public boolean equals(final Object obj) {
    if(obj == this) return true;
    if(!(obj instanceof DoubleList)) return false;
    final DoubleList dl = (DoubleList) obj;
    if(size != dl.size) return false;
    for(int l = 0; l < size; ++l) {
      if(list[l] != dl.list[l]) return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return list == null ? "" : Arrays.toString(toArray());
  }
}
