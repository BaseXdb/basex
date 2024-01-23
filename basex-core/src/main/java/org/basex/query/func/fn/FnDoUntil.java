package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public class FnDoUntil extends FnWhileDo {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final FItem action = toFunction(arg(1), 2, qc), predicate = toFunction(arg(2), 2, qc);
    Value value = arg(0).value(qc);

    int p = 0;
    while(true) {
      final Int pos = Int.get(++p);
      value = action.invoke(qc, info, value, pos);
      if(toBoolean(qc, predicate, value, pos)) return value;
    }
  }
}
