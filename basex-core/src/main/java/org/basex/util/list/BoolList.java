package org.basex.util.list;

import java.util.*;

import org.basex.util.*;

/**
 * Resizable-array implementation for native booleans.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class BoolList extends ElementList {
  /** Element container. */
  private boolean[] list;

  /**
   * Default constructor.
   */
  public BoolList() {
    this(-1);
  }

  /**
   * Constructor with initial capacity.
   * @param capacity array capacity
   */
  public BoolList(final long capacity) {
    list = new boolean[Array.initialCapacity(capacity)];
  }

  /**
   * Adds an element.
   * @param element element to be added
   * @return self reference
   */
  public BoolList add(final boolean element) {
    boolean[] lst = list;
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
  public BoolList add(final boolean... elements) {
    boolean[] lst = list;
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
   * Returns the element at the specified index.
   * @param index index of the element to return
   * @return element
   */
  public boolean get(final int index) {
    return list[index];
  }

  /**
   * Stores an element at the specified index.
   * @param index index of the element to replace
   * @param element element to be stored
   */
  public void set(final int index, final boolean element) {
    if(index >= list.length) list = Arrays.copyOf(list, newCapacity(index + 1));
    list[index] = element;
    size = Math.max(size, index + 1);
  }

  /**
   * Pops the uppermost element from the stack.
   * @return the popped element
   */
  public boolean pop() {
    return list[--size];
  }

  /**
   * Pushes an element onto the stack.
   * @param element element
   */
  public void push(final boolean element) {
    add(element);
  }

  /**
   * Returns the uppermost element on the stack, without removing it.
   * @return uppermost element
   */
  public boolean peek() {
    return list[size - 1];
  }

  /**
   * Finishes the int array.
   * @return int array
   */
  public boolean[] toArray() {
    return Arrays.copyOf(list, size);
  }

  /**
   * Returns an array with all elements and resets the array size.
   * @return array
   */
  public boolean[] next() {
    final boolean[] lst = Arrays.copyOf(list, size);
    reset();
    return lst;
  }

  /**
   * Returns an array with all elements and invalidates the internal array.
   * Warning: the function must only be called if the list is discarded afterward.
   * @return array (internal representation!)
   */
  public boolean[] finish() {
    final boolean[] lst = list;
    list = null;
    final int s = size;
    return s == lst.length ? lst : Arrays.copyOf(lst, s);
  }

  @Override
  public boolean equals(final Object obj) {
    return obj == this || obj instanceof final BoolList l &&
        Arrays.equals(list, 0, size, l.list, 0, l.size);
  }

  @Override
  public String toString() {
    return list == null ? "" : Arrays.toString(toArray());
  }
}
