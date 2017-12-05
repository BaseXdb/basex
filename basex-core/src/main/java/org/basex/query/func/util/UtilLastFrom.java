package org.basex.query.func.util;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.func.fn.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class UtilLastFrom extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    // fast route if the size is known
    final Iter iter = exprs[0].iter(qc);
    final long size = iter.size();
    if(size >= 0) return size == 0 ? null : iter.get(size - 1);

    // loop through all items
    Item last = null;
    for(Item item; (item = qc.next(iter)) != null;) last = item;
    return last;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr expr = exprs[0];
    final SeqType st = expr.seqType();
    if(st.zeroOrOne()) return expr;
    exprType.assign(st.type, st.oneOrMore() ? Occ.ONE : Occ.ZERO_ONE);

    // check for large values and fn:reverse function
    return expr instanceof Value ? ((Value) expr).itemAt(expr.size() - 1) :
           expr instanceof FnReverse ? cc.function(Function.HEAD, info, ((Arr) expr).exprs) :
           this;
  }
}
