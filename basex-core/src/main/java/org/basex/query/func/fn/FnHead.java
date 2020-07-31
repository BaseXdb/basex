package org.basex.query.func.fn;

import static org.basex.query.func.Function.*;
import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.List;
import org.basex.query.func.*;
import org.basex.query.func.file.*;
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

    // rewrite nested function calls
    final long size = expr.size();
    if(_UTIL_INIT.is(expr) && size > 1)
      return cc.function(HEAD, info, args(expr));
    if(TAIL.is(expr))
      return cc.function(_UTIL_ITEM, info, args(expr)[0], Int.get(2));
    if(SUBSEQUENCE.is(expr) || _UTIL_RANGE.is(expr)) {
      final SeqRange r = SeqRange.get(expr, cc);
      // safety check (due to previous optimizations, r.length will never be 0)
      if(r != null && r.length != 0)
        return cc.function(_UTIL_ITEM, info, args(expr)[0], Int.get(r.start + 1));
    }
    if(REVERSE.is(expr))
      return cc.function(_UTIL_LAST, info, args(expr));
    if(_FILE_READ_TEXT_LINES.is(expr))
      return FileReadTextLines.opt(this, 0, 1, cc);

    // rewrite list to its arguments or to elvis operator
    if(expr instanceof List) {
      final Expr[] args = ((List) expr).exprs;
      final SeqType st1 = args[0].seqType();
      if(st1.one())
        return args[0];
      if(st1.oneOrMore())
        return cc.function(HEAD, info, args[0]);
      if(st1.zeroOrOne()) {
        final Expr dflt = new List(info, Arrays.copyOfRange(args, 1, args.length)).optimize(cc);
        return cc.function(_UTIL_OR, info, args[0], cc.function(HEAD, info, dflt));
      }
    }

    exprType.assign(st.with(st.oneOrMore() ? Occ.ONE : Occ.ZERO_ONE));
    data(expr.data());
    return this;
  }
}
