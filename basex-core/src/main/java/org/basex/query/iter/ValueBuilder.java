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
  public Item[] item;
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
    item = new Item[c];
  }

  /**
   * Constructor.
   * @param arr initial array
   * @param s initial size
   */
  public ValueBuilder(final Item[] arr, final int s) {
    item = arr;
    size = s;
  }

  /**
   * Adds the contents of a value.
   * @param val value to be added
   * @return self reference
   */
  public ValueBuilder add(final Value val) {
    for(final long sz = val.size(); item.length - size < sz;) item = extend(item);
    size += val.writeTo(item, size);
    return this;
  }

  /**
   * Adds a single item.
   * @param it item to be added
   * @return self reference
   */
  public ValueBuilder add(final Item it) {
    if(size == item.length) item = extend(item);
    item[size++] = it;
    return this;
  }

  @Override
  public boolean sameAs(final Result v) {
    if(!(v instanceof ValueBuilder)) return false;

    final ValueBuilder vb = (ValueBuilder) v;
    if(size != vb.size) return false;
    for(int i = 0; i < size; ++i) {
      if(item[i].type != vb.item[i].type || !item[i].sameAs(vb.item[i])) return false;
    }
    return true;
  }

  @Override
  public void serialize(final Serializer ser) throws IOException {
    for(int c = 0; c < size && !ser.finished(); ++c) serialize(ser, c);
  }

  @Override
  public void serialize(final Serializer ser, final int n) throws IOException {
    ser.serialize(item[n]);
  }

  @Override
  public Item next() {
    return ++pos < size ? item[pos] : null;
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

  @Override
  public Item get(final long i) {
    return item[(int) i];
  }

  /**
   * Sets an item to the specified position.
   * @param i item to be set
   * @param p position
   */
  public void set(final Item i, final int p) {
    item[p] = i;
  }

  @Override
  public Value value() {
    return Seq.get(item, size);
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
