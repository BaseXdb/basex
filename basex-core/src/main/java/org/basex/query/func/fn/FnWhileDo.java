package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public class FnWhileDo extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final FItem predicate = toFunction(arg(1), 2, qc), action = toFunction(arg(2), 2, qc);

    final HofArgs args = new HofArgs(2, predicate, action).set(0, arg(0).value(qc));
    while(true) {
      if(!test(predicate, args.inc(), qc)) return args.get(0);
      args.set(0, invoke(action, args, qc));
    }
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final boolean until = this instanceof FnDoUntil;
    final int p = until ? 2 : 1, a = until ? 1 : 2;
    final Expr input = arg(0), action = arg(a);

    if(arg(a) instanceof FuncItem) {
      SeqType st = input.seqType(), ost;
      do {
        final SeqType[] types = { st, SeqType.INTEGER_O };
        arg(a, arg -> refineFunc(action, cc, types));
        ost = st;
        st = st.union(arg(a).funcType().declType);
      } while(!st.eq(ost));

      if(arg(p) instanceof FuncItem) {
        final SeqType[] types = { st, SeqType.INTEGER_O };
        arg(p, arg -> refineFunc(arg(p), cc, types));
        if(!until && ((FuncItem) arg(p)).expr == Bln.FALSE) return input;
      }

      exprType.assign(st);
    }
    return this;
  }
}
