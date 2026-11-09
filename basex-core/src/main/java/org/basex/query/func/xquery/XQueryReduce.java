package org.basex.query.func.xquery;

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
public final class XQueryReduce extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value input = arg(0).value(qc);
    final Value init = arg(1).value(qc);
    final FItem action = toFunction(arg(2), 2, qc);
    final FItem combine = toFunction(arg(3), 2, qc);
    final TaskOptions options = options(4, TaskOptions::new, qc);

    if(input.isEmpty()) return init;

    final TaskContext tc = new TaskContext(options, qc, info);
    return tc.invoke(new ReduceTask(tc, input, init, action, combine));
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr input = arg(0), init = arg(1), action = arg(2), combine = arg(3);
    final SeqType ist = input.seqType();
    // xquery:reduce((), $init, $action, $combine) → $init
    if(ist.zero()) return init;
    optOptions(4, TaskOptions::new, cc);

    // action: called with the seed value or its own results
    final SeqType zst = init.seqType(), itemType = ist.with(Occ.EXACTLY_ONE);
    SeqType st = zst, ost, rt;
    do {
      final SeqType[] types = { st, itemType };
      arg(2, arg -> action.refineFunc(cc, types));
      rt = refinedType(2);
      ost = st;
      st = st.union(rt);
    } while(!st.eq(ost));

    // combine: called with partial results (chunks are never empty)
    SeqType pst = rt;
    do {
      final SeqType[] types = { pst, pst };
      arg(3, arg -> combine.refineFunc(cc, types));
      ost = pst;
      pst = pst.union(refinedType(3));
    } while(!pst.eq(ost));

    exprType.assign(ist.oneOrMore() ? pst : zst.union(pst));
    return this;
  }

  /**
   * Returns the refined return type of a function argument.
   * @param a index of the argument
   * @return return type
   */
  private SeqType refinedType(final int a) {
    final FuncType ft = arg(a).funcType();
    return ft != null ? ft.refinedType : Types.ITEM_ZM;
  }
}
