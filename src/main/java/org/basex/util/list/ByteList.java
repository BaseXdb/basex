package org.basex.util.list;

import static org.basex.util.Token.*;

import java.util.*;

/**
 * This is a simple container for byte values.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public class ByteList extends ElementList {
  /** Element container. */
  protected byte[] list;

  /**
   * Default constructor.
   */
  public ByteList() {
    this(CAP);
  }

  /**
   * Constructor, specifying an initial array capacity.
   * @param c array capacity
   */
  public ByteList(final int c) {
    list = new byte[c];
  }

  /**
   * Adds an entry to the array.
   * @param e entry to be added
   * @return self reference
   */
  public ByteList add(final int e) {
    if(size == list.length) list = Arrays.copyOf(list, newSize());
    list[size++] = (byte) e;
    return this;
  }

  /**
   * Adds a byte array to the container.
   * @param b the entries to be added
   * @return self reference
   */
  public ByteList add(final byte[] b) {
    return add(b, 0, b.length);
  }

  /**
   * Adds a partial byte array to the container.
   * @param b the entries to be added
   * @param s start position
   * @param e end position
   * @return self reference
   */
  public ByteList add(final byte[] b, final int s, final int e) {
    final int l = e - s;
    if(size + l > list.length) list = Arrays.copyOf(list, newSize(size + l));
    System.arraycopy(b, s, list, size, l);
    size += l;
    return this;
  }

  /**
   * Returns the element at the specified index position.
   * @param i index
   * @return element
   */
  public final byte get(final int i) {
    return list[i];
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
