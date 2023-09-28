package org.basex.query.func.fn;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.hash.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class FnIndexWhere extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    // implementation for dynamic function lookup
    final Iter input = arg(0).iter(qc);
    final FItem predicate = toFunction(arg(1), 1, qc);

    int c = 0;
    final LongList list = new LongList();
    for(Item item; (item = input.next()) != null;) {
      ++c;
      if(toBoolean(eval(predicate, qc, item).item(qc, info))) {
        list.add(c);
      }
    }
    return IntSeq.get(list);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr input = arg(0);
    final SeqType st = input.seqType();
    if(st.zero()) return input;

    // rewrite to FLWOR expression
    // index-where(INPUT, PREDICATE)  ->  for $i at $p in INPUT where PREDICATE($i) return $p
    final IntObjMap<Var> vm = new IntObjMap<>();
    final LinkedList<Clause> clauses = new LinkedList<>();

    final Var i = cc.copy(new Var(new QNm("i"), null, cc.qc, sc, info), vm);
    final Var p = cc.copy(new Var(new QNm("p"), SeqType.INTEGER_O, cc.qc, sc, info), vm);
    clauses.add(new For(i, p, null, input, false).optimize(cc));

    final Expr pred = coerceFunc(arg(1), cc, SeqType.BOOLEAN_O, st.with(Occ.EXACTLY_ONE));
    final Expr arg = new VarRef(info, i).optimize(cc);
    clauses.add(new Where(new DynFuncCall(info, sc, pred, arg).optimize(cc), info).optimize(cc));

    return new GFLWOR(info, clauses, new VarRef(info, p).optimize(cc)).optimize(cc);
  }
}
