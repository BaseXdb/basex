package org.basex.query.iter;

import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.item.Seq;

/**
 * Iterator interface.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public abstract class Iter {
  /** Empty iterator. */
  public static final Iter EMPTY = new Iter() {
    @Override
    public Item next() { return null; }
    @Override
    public Item finish() { return Seq.EMPTY; }
    @Override
    public long size() { return 0; }
    @Override
    public boolean reset() { return true; }
  };

  /**
   * Returns the next item or null if no other items are found.
   * @return resulting item
   * @throws QueryException query exception
   */
  public abstract Item next() throws QueryException;

  /**
   * Returns the specified item. This method needs to be implemented if
   * {@link #size} returns the number of results, i.e., does not return
   * {@code -1}.
   * @param i value offset
   * @return specified item
   * @throws QueryException query exception
   */
  @SuppressWarnings("unused")
  public Item get(final long i) throws QueryException {
    return null;
  }

  /**
   * Returns the number of entries. Note: {@code -1} is returned if the
   * number is unknown, so the returned value has to be checked.
   * If this method is implemented by an iterator, {@link #get} needs to be
   * implemented as well.
   * @return number of entries
   * @throws QueryException query exception
   */
  @SuppressWarnings("unused")
  public long size()  throws QueryException {
    return -1;
  }

  /**
   * Resets the iterator and returns {@code true} if operation was successful.
   * {@code false} is returned if the iterator cannot be reversed.
   * @return true if operator could be reset
   */
  public boolean reset() {
    return false;
  }

  /**
   * Reverses the iterator and returns {@code true} if operation was successful.
   * {@code false} is returned if the iterator cannot be reversed.
   * @return true if operator could be reversed
   */
  public boolean reverse() {
    return false;
  }

  /**
   * Returns a sequence from all iterator values.
   * Should be called before {@link #next}.
   * @return sequence
   * @throws QueryException query exception
   */
  public Item finish() throws QueryException {
    Item i = next();
    if(i == null) return Seq.EMPTY;

    Item[] item = { i };
    int s = 1;
    while((i = next()) != null) {
      if(s == item.length) item = Item.extend(item);
      item[s++] = i;
    }
    return Seq.get(item, s);
  }
}
