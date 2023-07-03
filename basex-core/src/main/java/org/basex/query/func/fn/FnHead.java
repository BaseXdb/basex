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
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class FnHead extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item input = arg(0).iter(qc).next();
    return input == null ? Empty.VALUE : input;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr input = arg(0);
    final SeqType st = input.seqType();
    if(st.zeroOrOne()) return input;

    final long size = input.size();
    // head(tail(E))  ->  items-at(E, 2)
    if(TAIL.is(input))
      return cc.function(ITEMS_AT, info, input.arg(0), Int.get(2));
    // head(trunk(E))  ->  head(E)
    if(TRUNK.is(input) && size > 1)
      return cc.function(HEAD, info, input.args());
    // head(subsequence(E, pos))  ->  items-at(E, pos)
    if(SUBSEQUENCE.is(input) || _UTIL_RANGE.is(input)) {
      final SeqRange r = SeqRange.get(input, cc);
      // safety check (at this stage, r.length should never be 0)
      if(r != null && r.length != 0)
        return cc.function(ITEMS_AT, info, input.arg(0), Int.get(r.start + 1));
    }
    if(REVERSE.is(input)) {
      // head(reverse(root[test]))  ->  head(reverse(root)[test])
      if(input.arg(0) instanceof IterFilter) {
        final IterFilter filter = (IterFilter) input.arg(0);
        return cc.function(HEAD, info,
            Filter.get(cc, filter.info(), cc.function(REVERSE, info, filter.root), filter.exprs));
      }
      // head(reverse(E))  ->  foot(E)
      return cc.function(FOOT, info, input.args());
    }
    // head(replicate(E, count))  ->  head(E)
    if(REPLICATE.is(input)) {
      // static integer will always be greater than 1
      if(input.arg(1) instanceof Int) return cc.function(HEAD, info, input.arg(0));
    }
    // head(file:read-text-lines(E))  ->  file:read-text-lines(E, 0, 1)
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
        // head(($a[.], 1))         ->  $a[.] otherwise 1
        // head(($a[.], $b[.], 1))  ->  (will not be rewritten)
        final Expr dflt = List.get(cc, info, Arrays.copyOfRange(args, 1, al));
        return new Otherwise(info, first, cc.function(HEAD, info, dflt)).optimize(cc);
      }
    }

    final Occ occ = st.oneOrMore() ? Occ.EXACTLY_ONE : Occ.ZERO_OR_ONE;
    exprType.assign(st.with(occ)).data(input);
    return embed(cc, false);
  }
}
