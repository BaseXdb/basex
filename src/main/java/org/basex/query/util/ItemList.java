package org.basex.query.util;

import java.util.Arrays;
import org.basex.query.item.Item;

/**
 * This is a simple container for items.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Michael Seiferle
 */
public final class ItemList {
  /** Initial hash capacity. */
  private static final int CAP = 1 << 3;
  /** List entries. */
  public Item[] list = new Item[CAP];
  /** Number of entries. */
  public int size;

  /**
   * Adds an item to the list.
   * @param i Item to add
   */
  public void add(final Item i) {
    if(size == list.length) {
      final Item[] tmp = new Item[size << 1];
      System.arraycopy(list, 0, tmp, 0, size);
      list = tmp;
    }
    list[size++] = i;
  }

  /**
   * Returns the length of the list.
   * @return size
   */
  public int size() {
    return size;
  }

  /**
   * Returns the Item at position {@code i}, null if not found.
   * @param p position.
   * @return Item {@code i}
   */
  public Item get(final int p) {
    return list[p];
  }

  /* for debugging (should be removed later) */
  @Override
  public String toString() {
    return Arrays.toString(list);
  }
}
