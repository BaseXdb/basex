package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class FnItemsAfter extends FnItemsStartingWhere {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Iter input = arg(0).iter(qc);
    final FItem predicate = toFunction(arg(1), 2, qc);
    return new Iter() {
      boolean started;
      int p;

      @Override
      public Item next() throws QueryException {
        for(Item item; (item = input.next()) != null;) {
          if(started) return item;
          if(toBoolean(qc, predicate, item, Int.get(++p))) started = true;
        }
        return null;
      }
    };
  }
}
