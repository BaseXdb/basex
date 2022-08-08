package org.basex.query.func.fn;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.CmpG.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public class FnAll extends StandardFunc {
  @Override
  public final Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    // implementation for dynamic function lookup
    final Iter iter = exprs[0].iter(qc);
    final FItem func = toFunction(exprs[1], 1, qc);

    final boolean some = some();
    for(Item item; (item = qc.next(iter)) != null;) {
      if(toBoolean(func.invoke(qc, info, item).item(qc, info)) ^ !some) return Bln.get(some);
    }
    return Bln.get(!some);
  }

  @Override
  protected final Expr opt(final CompileContext cc) throws QueryException {
    final Expr items = exprs[0];
    final SeqType st = items.seqType();
    if(st.zero()) return cc.merge(items, Bln.TRUE, info);

    // create FLWOR expression
    final LinkedList<Clause> clauses = new LinkedList<>();
    final IntObjMap<Var> vm = new IntObjMap<>();
    final Var var = cc.copy(new Var(new QNm("item"), null, false, cc.qc, sc, info), vm);
    clauses.add(new For(var, items).optimize(cc));

    final Expr func = coerceFunc(exprs[1], cc, SeqType.BOOLEAN_O, st.with(Occ.EXACTLY_ONE));
    final ParseExpr ref = new VarRef(info, var).optimize(cc);
    final Expr rtrn = new DynFuncCall(info, sc, func, ref).optimize(cc);
    final Expr flwor = new GFLWOR(info, clauses, rtrn).optimize(cc);

    final boolean some = some();
    final CmpG cmp = new CmpG(flwor, Bln.get(some), OpG.EQ, null, sc, info);
    return some ? cmp : Function.NOT.get(sc, info, cmp);
  }

  /**
   * Compare some/all results.
   * @return flag
   */
  boolean some() {
    return false;
  }
}
