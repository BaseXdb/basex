package org.basex.util;

import static org.basex.util.Token.*;
import java.util.Arrays;

/**
 * This is a simple container for byte values.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public class ByteList extends ElementList {
  /** Element container. */
  protected byte[] list;

  /**
   * Default constructor.
   */
  public ByteList() {
    list = new byte[CAP];
  }

  /**
   * Adds an entry to the array.
   * @param e entry to be added
   * @return self reference
   */
  public final ByteList add(final byte e) {
    if(size == list.length) list = Arrays.copyOf(list, newSize());
    list[size++] = e;
    return this;
  }

  /**
   * Adds a byte array to the token.
   * @param b the character array to be added
   * @return self reference
   */
  public ByteList add(final byte[] b) {
    final int l = b.length;
    final int ll = list.length;
    if(size + l > ll) list = Arrays.copyOf(list, newSize(size + l));
    System.arraycopy(b, 0, list, size, l);
    size += l;
    return this;
  }

  /**
   * Returns an array with all elements.
   * @return array
   */
  public final byte[] toArray() {
    return Arrays.copyOf(list, size);
  }

  @Override
  public String toString() {
    return string(list, 0, size);
  }
}
