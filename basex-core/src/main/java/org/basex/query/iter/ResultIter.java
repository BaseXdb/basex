package org.basex.query.iter;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;

/**
 * Iterator over the final result of a query, which allows the last item to be streamed.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ResultIter extends Iter {
  /** Wrapped iterator. */
  private final Iter iter;
  /** Item that will be returned next (can be {@code null}). */
  private Item next;
  /** Error raised while looking ahead (can be {@code null}). */
  private QueryException error;
  /** Indicates if the next item has already been requested. */
  private boolean ahead;

  /**
   * Constructor.
   * @param iter wrapped iterator
   */
  public ResultIter(final Iter iter) {
    this.iter = iter;
  }

  @Override
  public Item next() throws QueryException {
    final Item item;
    if(ahead) {
      ahead = false;
      if(error != null) throw error;
      item = next;
      next = null;
    } else {
      item = iter.next();
    }
    if(item instanceof final Lazy lazy) {
      // look ahead: the last item will not be accessed again after it has been consumed
      ahead = true;
      try {
        next = iter.next();
        if(next == null) lazy.once();
      } catch(final QueryException ex) {
        // raise error after the current item has been serialized
        error = ex;
      }
    }
    return item;
  }

  @Override
  public Item get(final long i) throws QueryException {
    return iter.get(i);
  }

  @Override
  public long size() throws QueryException {
    return iter.size();
  }

  @Override
  public Value eagerValue() {
    return iter.eagerValue();
  }
}
