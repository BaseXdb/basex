package org.basex.query.xquery.iter;

import org.basex.query.xquery.XQException;
import org.basex.query.xquery.item.Item;
import org.basex.util.Array;

/**
 * Sequence Iterator.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class SeqIter extends Iter {
  /** Items. */
  public Item[] item;
  /** Size. */
  public int size;
  /** Iterator. */
  public int pos = -1;

  /**
   * Constructor.
   */
  public SeqIter() {
    item = new Item[1];
  }

  /**
   * Constructor.
   * @param iter iterator
   * @throws XQException evaluation exception
   */
  public SeqIter(final Iter iter) throws XQException {
    item = new Item[1];
    add(iter);
  }

  /**
   * Constructor.
   * @param it item array
   * @param s size
   * @return iterator
   */
  public static Iter get(final Item[] it, final int s) {
    return s == 0 ? Iter.EMPTY : new SeqIter(it, s);
  }

  /**
   * Constructor.
   * @param it item array
   * @param s size
   */
  private SeqIter(final Item[] it, final int s) {
    item = it;
    size = s;
  }

  /**
   * Adds the contents of an iterator.
   * @param iter entry to be added
   * @throws XQException evaluation exception
   */
  public void add(final Iter iter) throws XQException {
    if(iter.size() == 1) {
      add(iter.next());
      return;
    }
    
    Item i;
    while((i = iter.next()) != null) {
      final Iter it = i.iter();
      while((i = it.next()) != null) add(i);
    }
  }

  /**
   * Adds a single item.
   * @param it item to be added
   */
  public void add(final Item it) {
    if(size == item.length) resize();
    item[size++] = it;
  }

  /**
   * Resizes the sequence array.
   */
  private void resize() {
    final Item[] tmp = new Item[size << 1];
    System.arraycopy(item, 0, tmp, 0, size);
    item = tmp;
  }

  /**
   * Inserts an item at the specified position.
   * Note that the item must be no sequence.
   * @param i entry to be added
   * @param p insert position
   */
  public void insert(final Item i, final int p) {
    if(size == item.length) resize();
    Array.move(item, p, 1, size++);
    item[p] = i;
  }
  
  @Override
  public Item next() {
    return ++pos < size ? item[pos] : null;
  }

  @Override
  public void reset() {
    pos = -1;
  }

  @Override
  public long size() {
    return size;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("(");
    for(int v = 0; v != size; v++) {
      sb.append((v != 0 ? ", " : "") + item[v]);
      if(sb.length() > 15 && v + 1 != size) {
        sb.append(", ...");
        break;
      }
    }
    return sb.append(")").toString();
  }
}
