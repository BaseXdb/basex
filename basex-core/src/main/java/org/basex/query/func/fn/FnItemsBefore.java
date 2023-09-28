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
public final class FnItemsBefore extends FnItemsStartingWhere {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return new Iter() {
      final Iter input = arg(0).iter(qc);
      final FItem predicate = toFunction(arg(1), 1, qc);

      @Override
      public Item next() throws QueryException {
        Item item = input.next();
        if(item != null && toBoolean(eval(predicate, qc, item).item(qc, info))) item = null;
        return item;
      }
    };
  }
}
