package org.basex.query.iter;

import java.io.*;

import org.basex.data.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * This class can be used to build new sequences.
 * At the same time, it serves as an iterator.
 *
 * @author BaseX Team 2005-13, BSD License
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
   * @param c initial capacity
   */
  public ValueBuilder(final int c) {
    items = new Item[c];
  }

  /**
   * Constructor.
   * @param arr initial array
   * @param s initial size
   */
  public ValueBuilder(final Item[] arr, final int s) {
    items = arr;
    size = s;
  }

  /**
   * Adds the contents of a value.
   * @param val value to be added
   * @return self reference
   */
  public ValueBuilder add(final Value val) {
    for(final long sz = val.size(); items.length - size < sz;) items = extend(items);
    size += val.writeTo(items, size);
    return this;
  }

  /**
   * Adds a single item.
   * @param it item to be added
   * @return self reference
   */
  public ValueBuilder add(final Item it) {
    if(size == items.length) items = extend(items);
    items[size++] = it;
    return this;
  }

  @Override
  public boolean sameAs(final Result v) {
    if(!(v instanceof ValueBuilder)) return false;

    final ValueBuilder vb = (ValueBuilder) v;
    if(size != vb.size) return false;
    for(int i = 0; i < size; ++i) {
      if(items[i].type != vb.items[i].type || !items[i].sameAs(vb.items[i])) return false;
    }
    return true;
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
  public boolean reset() {
    pos = -1;
    return true;
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
   * @param i item to be set
   * @param p position
   */
  public void set(final Item i, final int p) {
    items[p] = i;
  }

  @Override
  public Value value() {
    return Seq.get(items, size);
  }

  @Override
  public ArrayOutput serialize() throws IOException {
    final ArrayOutput ao = new ArrayOutput();
    serialize(Serializer.get(ao));
    return ao;
  }

  @Override
  public String toString() {
    try {
      return serialize().toString();
    } catch(final IOException ex) {
      throw Util.notexpected(ex);
    }
  }
}
