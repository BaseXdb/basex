package org.basex.query.util;

import java.util.Arrays;
import org.basex.query.item.Value;

/**
 * This is a simple container for values.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class ValueList {
  /** Initial hash capacity. */
  private static final int CAP = 1 << 3;
  /** List entries. */
  public Value[] list = new Value[CAP];
  /** Number of entries. */
  public int size;

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

  /* for debugging (should be removed later) */
  @Override
  public String toString() {
    return Arrays.toString(list);
  }
}
