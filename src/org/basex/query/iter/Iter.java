package org.basex.query.iter;

import static org.basex.query.QueryText.*;
import org.basex.query.QueryException;
import org.basex.query.item.Item;
import org.basex.query.item.Seq;
import org.basex.query.util.Err;

/**
 * Iterator interface.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
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
    public boolean ordered() { return true; }
    @Override
    public int size() { return 0; }
    @Override
    public boolean reset() { return true; }
    @Override
    public String toString() { return "()"; }
  };
  
  /**
   * Returns the next item or null if no other items are found.
   * @return resulting item
   * @throws QueryException evaluation exception
   */
  public abstract Item next() throws QueryException;
  
  /**
   * Returns true if the sequence is ordered. False is returned as default.
   * @return result of check
   */
  public boolean ordered() {
    return false;
  }

  /**
   * Returns the specified item. Note: null is returned if the
   * item cannot be retrieved, so the returned value has to be checked.
   * @param i value offset
   * @return specified item
   */
  @SuppressWarnings("unused")
  public Item get(final long i) {
    return null;
  }

  /**
   * Returns the number of entries. Note: -1 is returned if the
   * number cannot be retrieved, so the returned value has to be checked.
   * If this method is implemented, {@link #get} has to be implemented as well.
   * @return number of entries
   */
  public int size() {
    return -1;
  }

  /**
   * Resets the iterator and returns true. Note: false is returned if the
   * iterator cannot be reset, so the returned value has to be checked.
   * @return true if operator could be reset
   */
  public boolean reset() {
    return false;
  }

  /**
   * Reverses the iterator and returns true. Note: false is returned if the
   * iterator cannot be reset, so the returned value has to be checked.
   * @return true if operator could be reversed
   */
  public boolean reverse() {
    return false;
  }

  /**
   * Returns a sequence from all iterator values.
   * Should be called before {@link #next}.
   * @return sequence
   * @throws QueryException evaluation exception
   */
  public Item finish() throws QueryException {
    Item i = next();
    if(i == null) return Seq.EMPTY;
    
    Item[] item = { i };
    int s = 1;
    while((i = next()) != null) {
      if(s == item.length) {
        final Item[] tmp = new Item[s << 2];
        System.arraycopy(item, 0, tmp, 0, s);
        item = tmp;
      }
      item[s++] = i;
    }
    return Seq.get(item, s);
  }

  /**
   * Checks if the specified iterator contains a single item.
   * Returns null, the first item or an exception.
   * @return item
   * @throws QueryException evaluation exception
   */
  public final Item atomic() throws QueryException {
    final long s = size();
    if(s == 1) return next();

    final Item it = next();
    if(it == null) return null;

    final Item n = next();
    if(n != null) Err.or(XPSEQ, "(" + it + "," + n +
        (next() != null ? ",..." : "") + ")");
    return it;
  }
}
