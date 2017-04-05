package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class FnFilter extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final FItem fun = checkArity(exprs[1], 1, qc);
    final Iter iter = qc.iter(exprs[0]);
    return new Iter() {
      @Override
      public Item next() throws QueryException {
        for(Item it; (it = iter.next()) != null;) {
          qc.checkStop();
          if(toBoolean(fun.invokeItem(qc, info, it))) return it;
        }
        return null;
      }
    };
  }
}
