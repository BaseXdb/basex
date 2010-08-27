package org.basex.query.iter;

import static org.basex.query.QueryTokens.*;
import java.io.IOException;
import java.util.Arrays;
import org.basex.data.Result;
import org.basex.data.Serializer;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.item.Seq;
import org.basex.query.item.Value;
import org.basex.util.TokenBuilder;

/**
 * Item iterator.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class ItemIter extends Iter implements Result {
  /** Items. */
  public Item[] item;
  /** Size. */
  private int size;
  /** Iterator. */
  private int pos = -1;

  /**
   * Constructor.
   */
  public ItemIter() {
    this(1);
  }

  /**
   * Constructor.
   * @param c initial capacity
   */
  public ItemIter(final int c) {
    item = new Item[c];
  }

  /**
   * Constructor.
   * @param it item array
   * @param s size
   */
  public ItemIter(final Item[] it, final int s) {
    item = it;
    size = s;
  }

  /**
   * Returns a new sequence iterator with the contents of the specified
   * iterator. The specified iterator is returned if it is already an
   * {@link ItemIter} sequence.
   * @param iter iterator
   * @return iterator
   * @throws QueryException query exception
   */
  public static ItemIter get(final Iter iter) throws QueryException {
    if(iter instanceof ItemIter) return (ItemIter) iter;
    // size is cast as less than 2^32 are expected
    final ItemIter ir = new ItemIter(Math.max(1, (int) iter.size()));
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
    if(size == item.length) item = Item.extend(item);
    item[size++] = it;
  }

  @Override
  public boolean equiv(final Result v) {
    if(!(v instanceof ItemIter)) return false;

    final ItemIter sb = (ItemIter) v;
    if(size != sb.size) return false;
    try {
      for(int i = 0; i < size; ++i) {
        /// it is safe to pass on null, as item has the same type
        if(item[i].type != sb.item[i].type || !item[i].equiv(null, sb.item[i]))
          return false;
      }
      return true;
    } catch(final QueryException ex) {
      return false;
    }
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
    return i < size ? item[(int) i] : null;
  }

  @Override
  public Value finish() {
    return Seq.get(item, size);
  }
  
  @Override
  public String toString() {
    return new TokenBuilder().add(Arrays.copyOf(item, size), SEP).toString();
  }  
}
