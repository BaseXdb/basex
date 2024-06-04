package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.func.*;
import org.basex.query.func.update.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.hash.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public class FnForEach extends StandardFunc {
  @Override
  public final Value value(final QueryContext qc) throws QueryException {
    final Iter input = arg(0).iter(qc);
    final FItem action = toFunction(arg(1), 2, this instanceof UpdateForEach, qc);

    int p = 0;
    final ValueBuilder vb = new ValueBuilder(qc);
    for(Item item; (item = input.next()) != null;) {
      vb.add(action.invoke(qc, info, item, Int.get(++p)));
    }
    return vb.value(this);
  }

  @Override
  protected final Expr opt(final CompileContext cc) throws QueryException {
    final Expr input = arg(0), action = arg(1);
    final SeqType st = input.seqType();
    if(st.zero()) return input;

    final int arity = arity(action);
    if(arity == 1 || arity == 2) {
      // for $i in INPUT return ACTION($i)
      // for $i at $p in INPUT return ACTION($i, $p)
      final IntObjMap<Var> vm = new IntObjMap<>();
      final Var i = cc.copy(new Var(new QNm("item"), null, cc.qc, info), vm);
      final Var p = arity != 1 ? cc.copy(new Var(new QNm("pos"), null, cc.qc, info), vm) : null;
      final For fr = new For(i, p, null, input, false).optimize(cc);

      final Expr act = coerce(1, cc, arity);
      final boolean updating = this instanceof UpdateForEach, ndt = act.has(Flag.NDT);
      final ExprList args = new ExprList(new VarRef(info, i).optimize(cc));
      if(arity == 2) args.add(new VarRef(info, p).optimize(cc));
      final Expr rtrn = new DynFuncCall(info, updating, ndt, act, args.finish()).optimize(cc);
      return new GFLWOR(info, fr, rtrn).optimize(cc);
    }
    return this;
  }

  @Override
  public int hofIndex() {
    return 1;
  }
}
