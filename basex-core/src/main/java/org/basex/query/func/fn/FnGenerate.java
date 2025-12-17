package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnGenerate extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final FItem step = toFunction(arg(1), 2, qc);

    return new Iter() {
      final HofArgs args = new HofArgs(2, step);
      Item item;

      @Override
      public Item next() throws QueryException {
        final Expr expr = item == null ? arg(0) : invoke(step, args.set(0, item).inc(), qc);
        item = expr.item(qc, info);
        return item != Empty.VALUE ? item : null;
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return iterValue(qc);
  }
}
