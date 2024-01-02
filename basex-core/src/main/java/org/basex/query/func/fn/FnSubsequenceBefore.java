package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class FnSubsequenceBefore extends FnSubsequenceStartingWhere {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Iter input = arg(0).iter(qc);
    final FItem predicate = toFunction(arg(1), 2, qc);
    return new Iter() {
      int p;

      @Override
      public Item next() throws QueryException {
        Item item = input.next();
        if(item != null) {
          if(toBoolean(qc, predicate, item, Int.get(++p))) item = null;
        }
        return item;
      }
    };
  }
}
