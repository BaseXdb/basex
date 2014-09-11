package org.basex.query.func.hof;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
public final class HofUntil extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final FItem pred = checkArity(exprs[0], 1, qc);
    final FItem fun = checkArity(exprs[1], 1, qc);
    Value v = qc.value(exprs[2]);
    while(!toBoolean(pred.invokeItem(qc, info, v))) v = fun.invokeValue(qc, info, v);
    return v;
  }
}
