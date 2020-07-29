package org.basex.query.func.util;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.func.file.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
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
public final class UtilItem extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    // retrieve (possibly invalid) position
    final long pos = pos(qc);
    if(pos < 0) return Empty.VALUE;

    // if possible, retrieve single item
    final Expr expr = exprs[0];
    if(expr.seqType().zeroOrOne()) return pos == 0 ? expr.item(qc, info) : Empty.VALUE;

    // fast route if the size is known
    final Iter iter = expr.iter(qc);
    final long size = iter.size();
    if(size >= 0) return pos < size ? iter.get(pos) : Empty.VALUE;

    // iterate until specified item is found
    long p = pos;
    for(Item item; (item = qc.next(iter)) != null;) {
      if(p-- == 0) return item;
    }
    return Empty.VALUE;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr expr = exprs[0], pos = exprs[1];
    final SeqType st = expr.seqType();
    if(st.zero()) return expr;

    if(pos instanceof Value) {
      // retrieve (possibly invalid) position
      final long p = pos(cc.qc);
      if(p < 0) return Empty.VALUE;

      // pre-evaluate single expression with static position
      if(st.zeroOrOne()) return p == 0 ? expr : Empty.VALUE;
      // pre-evaluate values
      final long size = expr.size();
      if(size != -1) {
        if(p + 1 == size) return cc.function(Function._UTIL_LAST, info, expr);
        if(p + 1 > size) return Empty.VALUE;
        if(Function.REVERSE.is(expr))
          return cc.function(Function._UTIL_ITEM, info, args(expr)[0], Int.get(size - p));
      }
      if(p == 0) return cc.function(Function.HEAD, info, expr);

      // rewrite nested function calls
      if(Function.TAIL.is(expr))
        return cc.function(Function._UTIL_ITEM, info, args(expr)[0], Int.get(p + 2));
      if(Function._FILE_READ_TEXT_LINES.is(expr))
        return FileReadTextLines.opt(this, p, 1, cc);
    }

    if(Function._UTIL_INIT.is(expr))
      return cc.function(Function._UTIL_ITEM, info, args(expr)[0], pos);

    exprType.assign(st.with(Occ.ZERO_ONE));
    data(expr.data());
    return this;
  }

  /**
   * Returns the item position (starting with 0).
   * @param qc query context
   * @return position or {@code -1}
   * @throws QueryException query exception
   */
  private long pos(final QueryContext qc) throws QueryException {
    final double dp = toDouble(exprs[1], qc);
    final long pos = (long) dp;
    return dp != pos ? -1 : pos - 1;
  }
}
