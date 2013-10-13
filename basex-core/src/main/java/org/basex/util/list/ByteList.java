package org.basex.util.list;

import static org.basex.util.Token.*;

import java.util.*;

import org.basex.util.*;

/**
 * This is a simple container for byte values.
 *
 * @author BaseX Team 2005-13, BSD License
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
    if(size == list.length) list = Arrays.copyOf(list, newSize());
    list[size++] = (byte) element;
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
    return Arrays.copyOf(list, size);
  }

  @Override
  public String toString() {
    return string(list, 0, size);
  }
}
