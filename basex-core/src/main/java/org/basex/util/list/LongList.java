package org.basex.util.list;

import java.util.*;

import org.basex.util.*;

/**
 * Resizable-array implementation for native long values.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public class LongList extends ElementList {
  /** Element container. */
  protected long[] list;

  /**
   * Default constructor.
   */
  public LongList() {
    this(Array.INITIAL_CAPACITY);
  }

  /**
   * Constructor with initial capacity.
   * @param capacity array capacity
   */
  public LongList(final long capacity) {
    list = new long[Array.checkCapacity(capacity)];
  }

  /**
   * Adds an element to the array.
   * @param element element to be added
   * @return self reference
   */
  public final LongList add(final long element) {
    long[] lst = list;
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
  public final LongList add(final long... elements) {
    long[] lst = list;
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
  public final long get(final int index) {
    return list[index];
  }

  /**
   * Returns the uppermost element from the stack.
   * @return the uppermost element
   */
  public final long peek() {
    return list[size - 1];
  }

  /**
   * Pops the uppermost element from the stack.
   * @return the popped element
   */
  public final long pop() {
    return list[--size];
  }

  /**
   * Pushes an element onto the stack.
   * @param element element
   */
  public final void push(final long element) {
    add(element);
  }

  /**
   * Returns an array with all elements.
   * @return array
   */
  public final long[] toArray() {
    return Arrays.copyOf(list, size);
  }

  /**
   * Returns an array with all elements and invalidates the internal array.
   * Warning: the function must only be called if the list is discarded afterwards.
   * @return array (internal representation!)
   */
  public long[] finish() {
    final long[] lst = list;
    list = null;
    final int s = size;
    return s == lst.length ? lst : Arrays.copyOf(lst, s);
  }

  /**
   * Sorts the data.
   * @return self reference
   */
  public LongList sort() {
    final int s = size;
    if(s > 1) Arrays.sort(list, 0, s);
    return this;
  }

  /**
   * Removes duplicate entries.
   * @return self reference
   */
  public LongList distinct() {
    if(!isEmpty()) {
      int i = 1;
      for(int j = 1; j < size; ++j) {
        while(j < size && list[i - 1] == list[j]) j++;
        if(j < size) list[i++] = list[j];
      }
      size = i;
    }
    return this;
  }

  @Override
  public boolean equals(final Object obj) {
    if(obj == this) return true;
    if(!(obj instanceof LongList)) return false;
    final LongList ll = (LongList) obj;
    if(size != ll.size) return false;
    for(int l = 0; l < size; ++l) {
      if(list[l] != ll.list[l]) return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return list == null ? "" : Arrays.toString(toArray());
  }
}
