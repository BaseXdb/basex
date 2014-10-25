package org.basex.util.list;

import static org.basex.util.Token.*;

import java.util.*;

import org.basex.util.*;

/**
 * This is a simple container for byte values.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public class ByteList extends ElementList {
  /** Element container. */
  protected byte[] list;

  /**
   * Default constructor.
   */
  public ByteList() {
    this(Array.CAPACITY);
  }

  /**
   * Constructor, specifying an initial internal array size.
   * @param capacity initial array capacity
   */
  public ByteList(final int capacity) {
    list = new byte[capacity];
  }

  /**
   * Adds an element to the array.
   * @param element element to be added; will be cast to a byte
   * @return self reference
   */
  public ByteList add(final int element) {
    byte[] lst = list;
    int s = size;
    if(s == lst.length) lst = Arrays.copyOf(lst, newSize());
    lst[s++] = (byte) element;
    list = lst;
    size = s;
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
    if(size + l > list.length) list = Arrays.copyOf(list, newSize(size + l));
    System.arraycopy(elements, start, list, size, l);
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

  @Override
  public String toString() {
    return string(list, 0, size);
  }
}
