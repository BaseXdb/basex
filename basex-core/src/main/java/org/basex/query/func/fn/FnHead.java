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
    final Item input = exprs[0].iter(qc).next();
    return input == null ? Empty.VALUE : input;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr input = exprs[0];
    final SeqType st = input.seqType();
    if(st.zeroOrOne()) return input;

    // rewrite nested function calls
    final long size = input.size();
    if(_UTIL_INIT.is(input) && size > 1)
      return cc.function(HEAD, info, input.args());
    if(TAIL.is(input))
      return cc.function(_UTIL_ITEM, info, input.arg(0), Int.get(2));
    if(SUBSEQUENCE.is(input) || _UTIL_RANGE.is(input)) {
      final SeqRange r = SeqRange.get(input, cc);
      // safety check (at this stage, r.length will never be 0)
      if(r != null && r.length != 0)
        return cc.function(_UTIL_ITEM, info, input.arg(0), Int.get(r.start + 1));
    }
    if(REVERSE.is(input))
      return cc.function(_UTIL_LAST, info, input.args());
    if(REPLICATE.is(input)) {
      // static integer will always be greater than 1
      if(input.arg(1) instanceof Int) return cc.function(HEAD, info, input.arg(0));
    }
    if(_FILE_READ_TEXT_LINES.is(input))
      return FileReadTextLines.opt(this, 0, 1, cc);

    // rewrite list to its arguments or to elvis operator
    if(input instanceof List) {
      final Expr[] args = input.args();
      final Expr first = args[0];
      final SeqType stFirst = first.seqType();
      // head((1, 2))  ->  1
      if(stFirst.one()) return first;
      // head((1 to 2), 3))  ->  head(1 to 2)
      if(stFirst.oneOrMore()) return cc.function(HEAD, info, first);
      final int al = args.length;
      if(stFirst.zeroOrOne() && (al == 2 || args[1].seqType().occ != Occ.ZERO_OR_ONE)) {
        // head(($a[.], 1))         ->  util:or($a[.], 1)
        // head(($a[.], $b[.], 1))  ->  (will not be rewritten)
        final Expr dflt = List.get(cc, info, Arrays.copyOfRange(args, 1, al));
        return new Otherwise(info, first, cc.function(HEAD, info, dflt)).optimize(cc);
      }
    }

    final Expr embedded = embed(cc, false);
    if(embedded != null) return embedded;

    exprType.assign(st.with(st.oneOrMore() ? Occ.EXACTLY_ONE : Occ.ZERO_OR_ONE));
    data(input.data());
    return this;
  }
}
