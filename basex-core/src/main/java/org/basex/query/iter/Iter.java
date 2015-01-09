package org.basex.query.iter;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Iterator interface.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public abstract class Iter {
  /**
   * Returns the next item or {@code null} if no other items are found.
   * @return resulting item or {@code null}
   * @throws QueryException query exception
   */
  public abstract Item next() throws QueryException;

  /**
   * Returns the specified item, or an arbitrary item if the index is invalid.
   * This method needs to be implemented - and should only be called - if
   * {@link Iter#size()} returns the correct number of results.
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
   */
  public long size() {
    return -1;
  }

  /**
   * Returns a value with all iterated items.
   * Must only be called if {@link #next} has not been called before.
   * @return sequence
   * @throws QueryException query exception
   */
  public Value value() throws QueryException {
    // check if sequence is empty
    Item i = next();
    if(i == null) return Empty.SEQ;

    // if possible, allocate array with final size, and add all single items
    Item[] item = new Item[Math.max(1, (int) size())];
    int s = 0;
    do {
      if(s == item.length) item = extend(item);
      item[s++] = i;
    } while((i = next()) != null);

    // create final value
    return Seq.get(item, s);
  }

  /**
   * Doubles the size of an item array.
   * @param it item array
   * @return resulting array
   */
  static Item[] extend(final Item[] it) {
    return Array.copy(it, new Item[Array.newSize(it.length)]);
  }
}
