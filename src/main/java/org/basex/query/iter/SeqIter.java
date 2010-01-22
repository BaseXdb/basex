package org.basex.query.iter;

import java.io.IOException;
import java.util.Arrays;
import org.basex.data.Result;
import org.basex.data.Serializer;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.item.Seq;

/**
 * Sequence iterator.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class SeqIter extends Iter implements Result {
  /** Query context. */
  private QueryContext ctx;
  /** Items. */
  public Item[] item;
  /** Size. */
  private int size;
  /** Iterator. */
  private int pos = -1;

  /**
   * Constructor.
   */
  public SeqIter() {
    this(1);
  }

  /**
   * Constructor.
   * @param c initial capacity
   */
  public SeqIter(final int c) {
    item = new Item[c];
  }

  /**
   * Constructor, specifying the query context.
   * @param c query context
   */
  public SeqIter(final QueryContext c) {
    this();
    ctx = c;
  }

  /**
   * Constructor.
   * @param it item array
   * @param s size
   */
  public SeqIter(final Item[] it, final int s) {
    item = it;
    size = s;
  }

  /**
   * Returns the argument, if it is a sequence iterator.
   * Otherwise, returns a new sequence iterator with the iterator contents.
   * @param iter iterator
   * @return iterator
   * @throws QueryException query exception
   */
  public static SeqIter get(final Iter iter) throws QueryException {
    if(iter instanceof SeqIter) return (SeqIter) iter;
    final SeqIter si = new SeqIter(Math.max(1, iter.size()));
    si.add(iter);
    return si;
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
    if(size == item.length) item = Arrays.copyOf(item, size << 1);
    item[size++] = it;
  }

  public boolean same(final Result v) {
    if(!(v instanceof SeqIter)) return false;

    final SeqIter sb = (SeqIter) v;
    if(size != sb.size) return false;
    try {
      for(int i = 0; i < size; i++) {
        if(item[i].type != sb.item[i].type ||
          !item[i].eq(sb.item[i])) return false;
      }
      return true;
    } catch(final QueryException ex) {
      return false;
    }
  }

  public void serialize(final Serializer ser) throws IOException {
    for(int c = 0; c < size && !ser.finished(); c++) serialize(ser, c);
  }

  public void serialize(final Serializer ser, final int n) throws IOException {
    ser.openResult();
    ctx.serialize(ser, item[n]);
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
  public int size() {
    return size;
  }

  @Override
  public Item get(final long i) {
    return i < size ? item[(int) i] : null;
  }

  @Override
  public Item finish() {
    return Seq.get(item, size);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    if(size != 1) sb.append("(");
    for(int v = 0; v != size; v++) {
      sb.append((v != 0 ? ", " : "") + item[v]);
      if(sb.length() > 15 && v + 1 != size) {
        sb.append(", ...");
        break;
      }
    }
    if(size != 1) sb.append(")");
    return sb.toString();
  }
}
