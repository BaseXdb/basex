package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class FnDoUntil extends FnWhileDo {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final FItem action = toFunction(arg(1), 2, qc), predicate = toFunction(arg(2), 2, qc);

    final HofArgs args = new HofArgs(2, predicate, action).set(0, arg(0).value(qc));
    while(true) {
      args.set(0, invoke(action, args.inc(), qc));
      if(test(predicate, args, qc)) return args.get(0);
    }
  }
}
