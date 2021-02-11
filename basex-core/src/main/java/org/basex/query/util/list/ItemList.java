package org.basex.query.util.list;

import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Resizable-array implementation for items.
 *
 * @author BaseX Team 2005-21, BSD License
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
   * Constructor with initial capacity.
   * @param capacity array capacity
   */
  public ItemList(final long capacity) {
    super(new Item[Array.checkCapacity(capacity)]);
  }

  /**
   * Adds all items of a value to the array.
   * @param value value to be added
   * @return self reference
   */
  public ItemList add(final Value value) {
    for(final Item item : value) add(item);
    return this;
  }

  /**
   * Returns a value containing the items in this list.
   * @return the value
   */
  public Value value() {
    return value((Type) null);
  }

  /**
   * Returns a value with the type of the given expression.
   * @param type type (can be {@code null}, only considered if new sequence is created)
   * @return the value
   */
  public Value value(final Type type) {
    return ItemSeq.get(list, size, type);
  }

  /**
   * Returns a value with the type of the given expression.
   * @param expr expression
   * @return the value
   */
  public Value value(final Expr expr) {
    return ItemSeq.get(list, size, expr);
  }

  @Override
  protected Item[] newArray(final int s) {
    return new Item[s];
  }
}
