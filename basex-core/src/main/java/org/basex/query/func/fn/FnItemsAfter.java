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
    return new Iter() {
      final Iter input = exprs[0].iter(qc);
      final FItem predicate = toFunction(exprs[1], 1, qc);
      boolean started;

      @Override
      public Item next() throws QueryException {
        for(Item item; (item = input.next()) != null;) {
          if(started) return item;
          if(toBoolean(predicate.invoke(qc, info, item).item(qc, info))) started = true;
        }
        return null;
      }
    };
  }
}
