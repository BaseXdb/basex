package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.func.file.*;
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
public final class FnHead extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item item = exprs[0].iter(qc).next();
    return item == null ? Empty.VALUE : item;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr expr = exprs[0];
    final SeqType st = expr.seqType();
    if(st.zeroOrOne()) return expr;

    // check for large values and fn:reverse function
    if(expr instanceof Value) return ((Value) expr).itemAt(0);

    // rewrite nested function calls
    final long size = expr.size();
    if(Function._UTIL_INIT.is(expr) && size > 1)
      return cc.function(Function.HEAD, info, args(expr));
    if(Function.TAIL.is(expr))
      return cc.function(Function._UTIL_ITEM, info, args(expr)[0], Int.get(2));
    if(Function.SUBSEQUENCE.is(expr) || Function._UTIL_RANGE.is(expr)) {
      final SeqRange r = SeqRange.get(expr, cc);
      // safety check (due to previous optimizations, r.length will never be 0)
      if(r != null && r.length != 0)
        return cc.function(Function._UTIL_ITEM, info, args(expr)[0], Int.get(r.start + 1));
    }
    if(Function.REVERSE.is(expr))
      return cc.function(Function._UTIL_LAST, info, args(expr));
    if(Function._FILE_READ_TEXT_LINES.is(expr))
      return FileReadTextLines.opt(this, 0, 1, cc);

    exprType.assign(st.type, st.oneOrMore() ? Occ.ONE : Occ.ZERO_ONE);
    data(expr.data());
    return this;
  }
}
