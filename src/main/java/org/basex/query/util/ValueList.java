package org.basex.query.util;

import java.util.Arrays;
import org.basex.query.item.Value;
import org.basex.util.ElementList;

/**
 * This is a simple container for values.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class ValueList extends ElementList {
  /** List entries. */
  Value[] list = new Value[CAP];

  /**
   * Adds a value to the list.
   * @param v value to be added
   */
  public void add(final Value v) {
    if(size == list.length) {
      final Value[] tmp = new Value[size << 1];
      System.arraycopy(list, 0, tmp, 0, size);
      list = tmp;
    }
    list[size++] = v;
  }

  /**
   * Returns the element at the specified index.
   * @param i index
   * @return element
   */
  public Value get(final int i) {
    return list[i];
  }

  /* for debugging (should be removed later) */
  @Override
  public String toString() {
    return Arrays.toString(list);
  }
}
