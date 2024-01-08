package org.basex.query.func.fn;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
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
public final class FnFilter extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Iter input = arg(0).iter(qc);
    final FItem predicate = toFunction(arg(1), 2, qc);

    int p = 0;
    final ValueBuilder vb = new ValueBuilder(qc);
    for(Item item; (item = input.next()) != null;) {
      if(toBoolean(qc, predicate, item, Int.get(++p))) vb.add(item);
    }
    return vb.value(this);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr input = arg(0), predicate = arg(1);
    final SeqType st = input.seqType();
    if(st.zero()) return input;

    final int arity = arity(predicate);
    if(arity == 1) {
      // INPUT[PREDICATE(.)]
      final Expr pred = cc.get(input, () ->
        new DynFuncCall(info, sc, coerce(1, cc, 1), ContextValue.get(cc, info)).optimize(cc)
      );
      return Filter.get(cc, info, input, pred);
    } else if(arity == 2) {
      // for $i at $p in INPUT where PREDICATE($i, $p) return $i
      final IntObjMap<Var> vm = new IntObjMap<>();
      final LinkedList<Clause> clauses = new LinkedList<>();

      final Var i = cc.copy(new Var(new QNm("item"), null, cc.qc, sc, info), vm);
      final Var p = cc.copy(new Var(new QNm("pos"), SeqType.INTEGER_O, cc.qc, sc, info), vm);
      clauses.add(new For(i, p, null, input, false).optimize(cc));

      final Expr pred = coerce(1, cc);
      final Expr item = new VarRef(info, i).optimize(cc);
      final Expr pos = new VarRef(info, p).optimize(cc);
      final Expr dfc = new DynFuncCall(info, sc, pred, item, pos).optimize(cc);
      clauses.add(new Where(dfc, info).optimize(cc));

      return new GFLWOR(info, clauses, new VarRef(info, i).optimize(cc)).optimize(cc);
    }
    return this;
  }
}
