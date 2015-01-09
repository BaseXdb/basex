package org.basex.query.iter;

import java.io.*;

import org.basex.data.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.value.*;
import org.basex.query.value.array.Array;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * This class can be used to build new sequences.
 * At the same time, it may serve as an iterator.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class ValueBuilder extends ValueIter implements Result {
  /** Item container. */
  private Item[] items;
  /** Number of items. */
  private int size;
  /** Current iterator position. */
  private int pos = -1;

  /**
   * Constructor.
   */
  public ValueBuilder() {
    this(1);
  }

  /**
   * Constructor.
   * @param capacity initial capacity
   */
  public ValueBuilder(final int capacity) {
    items = new Item[capacity];
  }

  /**
   * Constructor.
   * @param items initial items
   * @param size initial size
   */
  public ValueBuilder(final Item[] items, final int size) {
    this.items = items;
    this.size = size;
  }

  /**
   * Adds the contents of a value.
   * @param value value to be added
   * @return self reference
   */
  public ValueBuilder add(final Value value) {
    if(value instanceof Item) return add((Item) value);

    final int s = size;
    Item[] tmp = items;
    for(final long sz = value.size(); tmp.length - s < sz;) tmp = extend(tmp);
    size = s + value.writeTo(tmp, s);
    items = tmp;
    return this;
  }

  /**
   * Adds a single item.
   * @param item item to be added
   * @return self reference
   */
  public ValueBuilder add(final Item item) {
    final int s = size;
    Item[] tmp = items;
    if(s == tmp.length) tmp = extend(tmp);
    tmp[s] = item;
    size = s + 1;
    items = tmp;
    return this;
  }

  /**
   * Adds flattened arrays.
   * @param it current item
   */
  public void addFlattened(final Item it) {
    if(it instanceof Array) {
      for(final Value v : ((Array) it).members()) {
        for(final Item i : v) addFlattened(i);
      }
    } else {
      add(it);
    }
  }

  @Override
  public void serialize(final Serializer ser) throws IOException {
    for(int c = 0; c < size && !ser.finished(); ++c) serialize(ser, c);
  }

  @Override
  public void serialize(final Serializer ser, final int n) throws IOException {
    ser.serialize(items[n]);
  }

  @Override
  public Item next() {
    return ++pos < size ? items[pos] : null;
  }

  /**
   * Sets the iterator size.
   * @param s size
   */
  public void size(final int s) {
    size = s;
  }

  @Override
  public long size() {
    return size;
  }

  /**
   * Returns the internal item container.
   * @return items
   */
  public Item[] items() {
    return items;
  }

  @Override
  public Item get(final long i) {
    return items[(int) i];
  }

  /**
   * Sets an item to the specified position.
   * @param i index
   * @param item item to be set
   */
  public void set(final int i, final Item item) {
    items[i] = item;
  }

  /**
   * Returns the cached items as value.
   * @return sequence (internal representation!)
   */
  @Override
  public Value value() {
    return Seq.get(items, size);
  }

  @Override
  public String serialize() throws IOException {
    final ArrayOutput ao = new ArrayOutput();
    serialize(Serializer.get(ao));
    return ao.toString();
  }

  @Override
  public String toString() {
    try {
      return serialize();
    } catch(final IOException ex) {
      throw Util.notExpected(ex);
    }
  }
}
