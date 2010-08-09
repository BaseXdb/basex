package org.basex.query.iter;

import org.basex.query.QueryException;
import org.basex.query.item.Empty;
import org.basex.query.item.Item;
import org.basex.query.item.Seq;
import org.basex.query.item.Value;

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
    public Value finish() { return Empty.SEQ; }
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
   * Returns the specified item, or an arbitrary item if the index is invalid.
   * This method needs to be implemented - and should only be called - if
   * {@link #size} returns the correct number of results. A calling method
   * should call {@link #reset} after the last items has been retrieved.
   * @param i value offset
   * @return specified item
   * @throws QueryException query exception
   */
  @SuppressWarnings("unused")
  public Item get(final long i) throws QueryException {
    return null;
  }

  /**
   * Returns the iterator size. Note: {@code -1} is returned if the
   * result size is unknown. If this method is implemented by an iterator,
   * {@link #get} needs to be implemented as well.
   * @return number of entries
   * @throws QueryException query exception
   */
  @SuppressWarnings("unused")
  public long size()  throws QueryException {
    return -1;
  }

  /**
   * Resets the iterator and returns {@code true} if operation was successful.
   * {@code false} is returned if the iterator cannot be reset.
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
  public Value finish() throws QueryException {
    Item i = next();
    if(i == null) return Empty.SEQ;

    Item[] item = { i };
    int s = 1;
    while((i = next()) != null) {
      if(s == item.length) item = Item.extend(item);
      item[s++] = i;
    }
    return Seq.get(item, s);
  }
}
