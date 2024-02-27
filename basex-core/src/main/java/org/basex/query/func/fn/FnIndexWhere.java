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
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class FnIndexWhere extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Iter input = arg(0).iter(qc);
    final FItem predicate = toFunction(arg(1), 2, qc);

    int p = 0;
    final LongList list = new LongList();
    for(Item item; (item = input.next()) != null;) {
      if(toBoolean(qc, predicate, item, Int.get(++p))) list.add(p);
    }
    return IntSeq.get(list.finish());
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr input = arg(0), predicate = arg(1);
    final SeqType st = input.seqType();
    if(st.zero()) return input;

    final int arity = arity(predicate);
    if(arity >= 1) {
      // for $i at $p in INPUT where PREDICATE($i, $p) return $p
      final IntObjMap<Var> vm = new IntObjMap<>();
      final LinkedList<Clause> clauses = new LinkedList<>();

      final Var i = cc.copy(new Var(new QNm("item"), null, cc.qc, info), vm);
      final Var p = cc.copy(new Var(new QNm("pos"), SeqType.INTEGER_O, cc.qc, info), vm);
      clauses.add(new For(i, p, null, input, false).optimize(cc));

      final Expr item = new VarRef(info, i).optimize(cc);
      final Expr pos = new VarRef(info, p).optimize(cc);
      final Expr[] args = arity == 1 ? new Expr[] { item } : new Expr[] { item, pos };
      final Expr dfc = new DynFuncCall(info, sc, coerce(1, cc, arity), args).optimize(cc);
      clauses.add(new Where(dfc, info).optimize(cc));

      return new GFLWOR(info, clauses, new VarRef(info, p).optimize(cc)).optimize(cc);
    }
    return this;
  }

  @Override
  public int hofIndex() {
    return 1;
  }
}
