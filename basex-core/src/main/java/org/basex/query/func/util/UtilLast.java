package org.basex.query.func.util;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-20, BSD License
 * @author Christian Gruen
 */
public final class UtilLast extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    // fast route if the size is known
    final Iter iter = exprs[0].iter(qc);
    final long size = iter.size();
    if(size >= 0) return size > 0 ? iter.get(size - 1) : Empty.VALUE;

    // loop through all items
    Item last = null;
    for(Item item; (item = qc.next(iter)) != null;) last = item;
    return last == null ? Empty.VALUE : last;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr expr = exprs[0];
    final SeqType st = expr.seqType();
    if(st.zeroOrOne()) return expr;

    // rewrite nested function calls
    final long size = expr.size();
    if(Function.TAIL.is(expr) && size > 1)
      return cc.function(Function._UTIL_LAST, info, args(expr));
    if(Function._UTIL_INIT.is(expr) && size > 0)
      return cc.function(Function._UTIL_ITEM, info, args(expr)[0], Int.get(size));
    if(Function.REVERSE.is(expr))
      return cc.function(Function.HEAD, info, args(expr));

    exprType.assign(st.with(st.oneOrMore() ? Occ.ONE : Occ.ZERO_ONE));
    data(expr.data());
    return this;
  }
}
