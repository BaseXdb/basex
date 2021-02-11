package org.basex.query.iter;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * Iterator interface.
 *
 * @author BaseX Team 2005-21, BSD License
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
   * @param i value offset (starting with 0)
   * @return specified item or {@code null}
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
  public Value iterValue() {
    return null;
  }

  /**
   * Returns a value with all iterated items. This method should always be called before single
   * items have been requested. Otherwise, it might not return all items.
   * @param qc query context
   * @param expr original expression (can be {@code null}; if assigned,
   *   type of result sequence will be refined)
   * @return value
   * @throws QueryException query exception
   */
  public Value value(final QueryContext qc, final Expr expr) throws QueryException {
    // check if sequence is empty
    final Item item1 = next();
    if(item1 == null) return Empty.VALUE;

    // check for single result
    final Item item2 = next();
    if(item2 == null) return item1;

    // more results: build sequence
    final ValueBuilder vb = new ValueBuilder(qc, item1, item2);
    for(Item item; (item = qc.next(this)) != null;) vb.add(item);
    return vb.value(expr);
  }
}
