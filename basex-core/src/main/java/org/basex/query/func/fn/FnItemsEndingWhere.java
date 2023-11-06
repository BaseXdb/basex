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
public final class FnItemsEndingWhere extends FnItemsStartingWhere {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return new Iter() {
      final Iter input = arg(0).iter(qc);
      final FItem predicate = toFunction(arg(1), 1, qc);
      boolean ended;

      @Override
      public Item next() throws QueryException {
        final Item item = ended ? null : input.next();
        if(item != null && toBoolean(predicate.invoke(qc, info, item).item(qc, info))) {
          ended  = true;
        }
        return item;
      }
    };
  }
}
