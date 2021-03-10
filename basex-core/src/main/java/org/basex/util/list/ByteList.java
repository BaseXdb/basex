package org.basex.util.list;

import static org.basex.util.Token.*;

import java.util.*;

import org.basex.util.*;

/**
 * Resizable-array implementation for native bytes.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public class ByteList extends ElementList {
  /** Element container. */
  protected byte[] list;

  /**
   * Default constructor.
   */
  public ByteList() {
    this(Array.INITIAL_CAPACITY);
  }

  /**
   * Constructor with initial array capacity.
   * @param capacity array capacity
3   */
  public ByteList(final long capacity) {
    list = new byte[Array.checkCapacity(capacity)];
  }

  /**
   * Adds an element to the array.
   * @param element element to be added; will be cast to a byte
   * @return self reference
   */
  public ByteList add(final int element) {
    byte[] lst = list;
    final int s = size;
    if(s == lst.length) lst = Arrays.copyOf(lst, newCapacity());
    lst[s] = (byte) element;
    list = lst;
    size = s + 1;
    return this;
  }

  /**
   * Adds elements to the container.
   * @param elements elements to be added
   * @return self reference
   */
  public ByteList add(final byte[] elements) {
    return add(elements, 0, elements.length);
  }

  /**
   * Adds a part of the specified elements to the container.
   * @param elements elements to be added
   * @param start start position
   * @param end end position
   * @return self reference
   */
  public ByteList add(final byte[] elements, final int start, final int end) {
    final int l = end - start;
    if(size + l > list.length) list = Arrays.copyOf(list, newCapacity(size + l));
    Array.copy(elements, start, l, list, size);
    size += l;
    return this;
  }

  /**
   * Returns the element at the specified index position.
   * @param index index of the element to return
   * @return element
   */
  public final byte get(final int index) {
    return list[index];
  }

  /**
   * Returns an array with all elements.
   * @return array
   */
  public byte[] toArray() {
    final int s = size;
    return s == 0 ? EMPTY : Arrays.copyOf(list, s);
  }

  /**
   * Returns an array with all elements and resets the array size.
   * @return array
   */
  public final byte[] next() {
    final int s = size;
    if(s == 0) return EMPTY;
    size = 0;
    return Arrays.copyOf(list, s);
  }

  /**
   * Returns an array with all elements and invalidates the internal array.
   * Warning: the function must only be called if the list is discarded afterwards.
   * @return array (internal representation!)
   */
  public byte[] finish() {
    final byte[] lst = list;
    list = null;
    final int s = size;
    return s == 0 ? EMPTY : s == lst.length ? lst : Arrays.copyOf(lst, s);
  }

  /**
   * Reverses the order of the elements.
   * @return self reference
   */
  public ByteList reverse() {
    byte[] lst = list;
    for(int l = 0, r = size - 1; l < r; l++, r--) {
      final byte tmp = lst[l];
      lst[l] = lst[r];
      lst[r] = tmp;
    }
    return this;
  }

  @Override
  public boolean equals(final Object obj) {
    if(obj == this) return true;
    if(!(obj instanceof ByteList)) return false;
    final ByteList bl = (ByteList) obj;
    if(size != bl.size) return false;
    for(int l = 0; l < size; ++l) {
      if(list[l] != bl.list[l]) return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return list == null ? "" : string(list, 0, size);
  }
}
