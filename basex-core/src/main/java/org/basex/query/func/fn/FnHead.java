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
 * @author BaseX Team 2005-22, BSD License
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
      return cc.function(HEAD, info, expr.args());
    if(TAIL.is(expr))
      return cc.function(_UTIL_ITEM, info, expr.arg(0), Int.get(2));
    if(SUBSEQUENCE.is(expr) || _UTIL_RANGE.is(expr)) {
      final SeqRange r = SeqRange.get(expr, cc);
      // safety check (at this stage, r.length will never be 0)
      if(r != null && r.length != 0)
        return cc.function(_UTIL_ITEM, info, expr.arg(0), Int.get(r.start + 1));
    }
    if(REVERSE.is(expr))
      return cc.function(_UTIL_LAST, info, expr.args());
    if(REPLICATE.is(expr)) {
      // static integer will always be greater than 1
      if(expr.arg(1) instanceof Int) return cc.function(HEAD, info, expr.arg(0));
    }
    if(_FILE_READ_TEXT_LINES.is(expr))
      return FileReadTextLines.opt(this, 0, 1, cc);

    // rewrite list to its arguments or to elvis operator
    if(expr instanceof List) {
      final Expr[] args = expr.args();
      final Expr first = args[0];
      final SeqType st1 = first.seqType();
      // head((1, 2))  ->  1
      if(st1.one()) return first;
      // head((1 to 2), 3))  ->  head(1 to 2)
      if(st1.oneOrMore()) return cc.function(HEAD, info, first);
      final int al = args.length;
      if(st1.zeroOrOne() && (al == 2 || args[1].seqType().occ != Occ.ZERO_OR_ONE)) {
        // head(($a[.], 1))         ->  util:or($a[.], 1)
        // head(($a[.], $b[.], 1))  ->  (will not be rewritten)
        final Expr dflt = List.get(cc, info, Arrays.copyOfRange(args, 1, al));
        return cc.function(_UTIL_OR, info, first, cc.function(HEAD, info, dflt));
      }
    }

    final Expr embedded = embed(cc, false);
    if(embedded != null) return embedded;

    exprType.assign(st.with(st.oneOrMore() ? Occ.EXACTLY_ONE : Occ.ZERO_OR_ONE));
    data(expr.data());
    return this;
  }
}
