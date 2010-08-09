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
   * @param it item to be added
   */
  public void add(final Item it) {
    if(size == list.length) list = Item.extend(list);
    list[size++] = it;
  }

  /* for debugging (should be removed later) */
  @Override
  public String toString() {
    return Arrays.toString(list);
  }
}
