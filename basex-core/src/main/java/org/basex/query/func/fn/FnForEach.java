package org.basex.query.func.fn;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.gflwor.*;
import org.basex.query.func.*;
import org.basex.query.func.update.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.hash.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public class FnForEach extends StandardFunc {
  @Override
  public final Value value(final QueryContext qc) throws QueryException {
    // implementation for dynamic function lookup
    final Iter iter = exprs[0].iter(qc);
    final FItem func = checkArity(exprs[1], 1, this instanceof UpdateForEach, qc);

    final ValueBuilder vb = new ValueBuilder(qc);
    for(Item item; (item = qc.next(iter)) != null;) vb.add(func.invoke(qc, info, item));
    return vb.value(this);
  }

  @Override
  protected final Expr opt(final CompileContext cc) throws QueryException {
    final Expr items = exprs[0];
    final SeqType st = items.seqType();
    if(st.zero()) return items;

    // create FLWOR expression
    final LinkedList<Clause> clauses = new LinkedList<>();
    final IntObjMap<Var> vm = new IntObjMap<>();
    final Var var = cc.copy(new Var(new QNm("each"), null, false, cc.qc, sc, info), vm);
    clauses.add(new For(var, items).optimize(cc));

    final Expr func = coerceFunc(exprs[1], cc, SeqType.ITEM_ZM, st.with(Occ.EXACTLY_ONE));
    final boolean updating = this instanceof UpdateForEach, ndt = func.has(Flag.NDT);
    final ParseExpr ref = new VarRef(info, var).optimize(cc);
    final Expr rtrn = new DynFuncCall(info, sc, updating, ndt, func, ref).optimize(cc);

    return new GFLWOR(info, clauses, rtrn).optimize(cc);
  }
}
