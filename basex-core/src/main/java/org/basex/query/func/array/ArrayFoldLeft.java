package org.basex.query.func.array;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.func.fn.*;
import org.basex.query.value.*;
import org.basex.query.value.array.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class ArrayFoldLeft extends FnFoldLeft {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final XQArray array = toArray(arg(0), qc);
    final FItem action = action(qc);

    final HofArgs args = new HofArgs(2).set(0, arg(1).value(qc));
    for(final Value value : array.iterable()) {
      args.set(1, value).inc();
      if(skip(qc, args)) break;
      args.set(0, invoke(action, args, qc));
    }
    return args.get(0);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    return optType(cc, true, true);
  }
}
