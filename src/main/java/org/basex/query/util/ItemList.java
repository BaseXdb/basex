package org.basex.query.util;

import java.util.Arrays;

import org.basex.query.item.Item;

/**
 * Simple container for quickly storing a list of items.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Michael Seiferle
 */
public final class ItemList {
  /** Initial hash capacity. */
  private static final int CAP = 1 << 3;
  /** Hash keys. */
  private int next = 0 << 0;
  /** Items. */
  private Item[] values = new Item[CAP];

  /** 
   * Creates an empty ItemList.
   */
  public ItemList() {

  }
  /**
   * Initializes an List with Item i.
   * @param i Item
   */
  public ItemList(final Item i) {
    add(i);
  }

  /**
   * Adds an item to the list.
   * @param i Item to add
   */
  public void add(final Item i) {
    values[next] = i;
    if(++next == values.length) resize();
  }

  /**
   * Resizes the item list.
   */
  private void resize() {
    final int s = values.length << 1;
    Item[] its = Arrays.copyOf(values, s);
    values = its;
  }

  /**
   * Returns the length of the list.
   * @return size
   */
  public int size() {
    return next;
  }
  /**
   * Returns the Item at position {@code i}, null if not found.
   * @param p position.
   * @return Item {@code i}
   */
  public Item get(final int p) {
    if(p >= next) return null;
    return values[p];
  }
  /**
   * Returns an Array representation of the Item list.
   * @return ItemList as Array
   */
  public Item[] toArray() {
    return Arrays.copyOf(values, size());
  }
}
