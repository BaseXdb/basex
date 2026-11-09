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
 * @author BaseX Team, BSD License
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
  protected final Expr opt(final CompileContext cc) throws QueryException {
    final boolean until = this instanceof FnDoUntil;
    final int p = until ? 2 : 1, a = until ? 1 : 2;
    final Expr input = arg(0), action = arg(a), predicate = arg(p);

    // type of the predicate argument and the result: do-until never tests or returns the input
    final SeqType ist = input.seqType();
    final SeqType st;
    if(action instanceof FuncItem || action instanceof Closure) {
      SeqType ast = ist, ost, rt;
      do {
        final SeqType[] types = { ast, Types.INTEGER_O };
        arg(a, arg -> action.refineFunc(cc, types));
        rt = arg(a).funcType().refinedType;
        ost = ast;
        ast = ast.union(rt);
      } while(!ast.eq(ost));
      st = until ? rt : ast;
    } else {
      final FuncType ft = action.funcType();
      if(ft == null) return this;
      st = until ? ft.refinedType : ist.union(ft.refinedType);
    }

    final SeqType[] types = { st, Types.INTEGER_O };
    arg(p, arg -> predicate.refineFunc(cc, types));
    // while-do(E, fn { false() }, ACTION) → E
    if(!until && arg(p) instanceof final FuncItem fi && fi.expr == Bln.FALSE) return input;

    exprType.assign(st);
    return this;
  }
}
