package org.basex.query.iter;

import java.io.IOException;
import org.basex.data.Result;
import org.basex.data.Serializer;
import org.basex.data.XMLSerializer;
import org.basex.io.ArrayOutput;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.item.Seq;
import org.basex.query.item.Value;
import org.basex.util.Util;

/**
 * Item iterator.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class ItemCache extends ValueIter implements Result {
  /** Item container. */
  public Item[] item;
  /** Number of items. */
  private int size;
  /** Current iterator position. */
  private int pos = -1;

  /**
   * Constructor.
   */
  public ItemCache() {
    this(1);
  }

  /**
   * Constructor.
   * @param c initial capacity
   */
  public ItemCache(final int c) {
    item = new Item[c];
  }

  /**
   * Constructor.
   * @param it item array
   * @param s size
   */
  public ItemCache(final Item[] it, final int s) {
    item = it;
    size = s;
  }

  /**
   * Returns a new sequence iterator with the contents of the specified
   * iterator. The specified iterator is returned if it is already an
   * {@link ItemCache} sequence.
   * @param iter iterator
   * @return iterator
   * @throws QueryException query exception
   */
  public static ItemCache get(final Iter iter) throws QueryException {
    if(iter instanceof ItemCache) return (ItemCache) iter;
    // size is cast as less than 2^32 are expected
    final ItemCache ir = new ItemCache(Math.max(1, (int) iter.size()));
    ir.add(iter);
    return ir;
  }

  /**
   * Adds the contents of an iterator.
   * @param iter entry to be added
   * @throws QueryException query exception
   */
  public void add(final Iter iter) throws QueryException {
    Item i;
    while((i = iter.next()) != null) add(i);
  }

  /**
   * Adds a single item.
   * @param it item to be added
   */
  public void add(final Item it) {
    if(size == item.length) item = extend(item);
    item[size++] = it;
  }

  @Override
  public boolean sameAs(final Result v) {
    if(!(v instanceof ItemCache)) return false;

    final ItemCache sb = (ItemCache) v;
    if(size != sb.size) return false;
    for(int i = 0; i < size; ++i) {
      if(item[i].type != sb.item[i].type || !item[i].sameAs(sb.item[i]))
        return false;
    }
    return true;
  }

  @Override
  public void serialize(final Serializer ser) throws IOException {
    for(int c = 0; c < size && !ser.finished(); ++c) serialize(ser, c);
  }

  @Override
  public void serialize(final Serializer ser, final int n) throws IOException {
    ser.openResult();
    item[n].serialize(ser);
    ser.closeResult();
  }

  @Override
  public Item next() {
    return ++pos < size ? item[pos] : null;
  }

  /**
   * Sets the iterator position.
   * @param p position
   */
  public void pos(final int p) {
    pos = p;
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

  @Override
  public Value finish() {
    return Seq.get(item, size);
  }

  @Override
  public String toString() {
    final ArrayOutput ao = new ArrayOutput();
    try {
      serialize(new XMLSerializer(ao));
    } catch(final IOException ex) {
      // [LW] is that OK? Example: (1, 2, upper-case#1)
      Util.notexpected(ex);
    }
    return ao.toString();
  }
}
