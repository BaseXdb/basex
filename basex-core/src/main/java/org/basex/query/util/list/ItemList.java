package org.basex.query.util.list;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Resizable-array implementation for items.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Leo Woerteler
 */
public final class ItemList extends ObjectList<Item, ItemList> {
  /**
   * Default constructor.
   */
  public ItemList() {
    this(1);
  }

  /**
   * Constructor, specifying an initial array capacity.
   * @param capacity array capacity
   */
  public ItemList(final int capacity) {
    super(new Item[capacity]);
  }

  /**
   * Adds all elements in the given value to this list.
   * @param value value to add
   * @return self reference
   */
  public ItemList add(final Value value) {
    if(value instanceof Item) return add((Item) value);
    final long n = value.size();
    if(n > Integer.MAX_VALUE - size) throw Util.notExpected(n);
    final int ns = size + (int) n;
    if(ns > list.length) list = Array.copy(list, newList(newSize(ns)));
    size += value.writeTo(list, size);
    return this;
  }

  /**
   * Returns a value containing the items in this list.
   * @return the value
   */
  public Value value() {
    return value(null);
  }

  /**
   * Returns a value with the given element type containing the items in this list.
   * @param type item type (not checked), may be {@code null}
   * @return the value
   */
  public Value value(final Type type) {
    return ValueBuilder.value(list, size, type);
  }

  /**
   * Returns an iterator over the items in this list.
   * The list must not be modified after the iterator has been requested.
   * @return the iterator
   */
  public BasicIter<Item> iter() {
    return new BasicIter<Item>(size) {
      @Override
      public Item get(final long i) {
        return list[(int) i];
      }
      @Override
      public Value value(final QueryContext qc) {
        return ItemList.this.value();
      }
    };
  }

  @Override
  protected Item[] newList(final int s) {
    return new Item[s];
  }
}
