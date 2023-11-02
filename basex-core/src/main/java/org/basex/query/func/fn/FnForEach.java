package org.basex.query.func.fn;

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
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public class FnForEach extends StandardFunc {
  @Override
  public final Value value(final QueryContext qc) throws QueryException {
    // implementation for dynamic function lookup
    final Iter input = arg(0).iter(qc);
    final FItem action = toFunction(arg(1), 1, this instanceof UpdateForEach, qc);

    final ValueBuilder vb = new ValueBuilder(qc);
    for(Item item; (item = input.next()) != null;) {
      vb.add(action.invoke(qc, info, item));
    }
    return vb.value(this);
  }

  @Override
  protected final Expr opt(final CompileContext cc) throws QueryException {
    final Expr input = arg(0);
    final SeqType st = input.seqType();
    if(st.zero()) return input;

    // create FLWOR expression
    // for-each(INPUT, ACTION)  ->  for $i in INPUT return ACTION($i)
    final Var var = cc.copy(new Var(new QNm("each"), null, cc.qc, sc, info), new IntObjMap<>());
    final For fr = new For(var, input).optimize(cc);

    final Expr func = coerceFunc(arg(1), cc, SeqType.ITEM_ZM, st.with(Occ.EXACTLY_ONE));
    final boolean updating = this instanceof UpdateForEach, ndt = func.has(Flag.NDT);
    final Expr ref = new VarRef(info, var).optimize(cc);
    final Expr rtrn = new DynFuncCall(info, sc, updating, ndt, func, ref).optimize(cc);
    return new GFLWOR(info, fr, rtrn).optimize(cc);
  }
}
