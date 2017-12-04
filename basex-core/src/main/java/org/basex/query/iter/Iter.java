package org.basex.query.iter;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * Iterator interface.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public abstract class Iter {
  /**
   * Returns the next item.
   * @return resulting item, or {@code null} if all items have been returned
   * @throws QueryException query exception
   */
  public abstract Item next() throws QueryException;

  /**
   * Returns the specified item, or an arbitrary item if the index is invalid.
   * If this method returns items, {@link #size()} needs to be implemented as well.
   * @param i value offset
   * @return specified item
   * @throws QueryException query exception
   */
  @SuppressWarnings("unused")
  public Item get(final long i) throws QueryException {
    return null;
  }

  /**
   * Returns the iterator size. {@code -1} is returned if the result size is unknown.
   * If this method returns a positive value, {@link #get(long)} needs to be implemented as well.
   * @return number of entries
   * @throws QueryException query exception
   */
  @SuppressWarnings("unused")
  public long size() throws QueryException {
    return -1;
  }

  /**
   * If available, returns a value on which the iterator is based on.
   * @return value or {@code null}
   */
  public Value value() {
    return null;
  }

  /**
   * Returns a value with all iterated items. This method returns all items
   * that have not been requested yet, or all values if the result size is known.
   * @param qc query context
   * @return value
   * @throws QueryException query exception
   */
  public Value value(final QueryContext qc) throws QueryException {
    // check if sequence is empty
    final Item i1 = next();
    if(i1 == null) return Empty.SEQ;

    // check for single result
    final Item i2 = next();
    if(i2 == null) return i1;

    // more results: build sequence
    final ValueBuilder vb = new ValueBuilder(qc).add(i1, i2);
    for(Item it; (it = qc.next(this)) != null;) vb.add(it);
    return vb.value();
  }
}
