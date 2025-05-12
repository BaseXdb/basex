package org.basex.query.func.hof;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Leo Woerteler
 */
public final class HofFoldLeft1 extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Iter input = arg(0).iter(qc);
    final FItem action = toFunction(arg(1), 3, qc);

    final Value first = input.next();
    if(first == null) return Empty.VALUE;

    final HofArgs args = new HofArgs(3).set(0, first);
    for(Item item; (item = input.next()) != null;) {
      args.set(0, invoke(action, args.set(1, item).inc(), qc));
    }
    return args.get(0);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr input = arg(0), action = arg(1);
    if(input.seqType().zero()) return input;

    final FuncType ft = action.funcType();
    if(ft != null) exprType.assign(ft.declType);
    return this;
  }

  @Override
  public int hofIndex() {
    return 1;
  }
}
