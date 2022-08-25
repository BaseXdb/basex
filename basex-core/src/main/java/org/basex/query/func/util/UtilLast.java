package org.basex.query.func.util;

import static org.basex.query.func.Function.*;

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
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class UtilLast extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    // fast route if the size is known
    final Iter input = exprs[0].iter(qc);
    final long size = input.size();
    if(size >= 0) return size > 0 ? input.get(size - 1) : Empty.VALUE;

    // loop through all items
    Item last = null;
    for(Item item; (item = qc.next(input)) != null;) {
      last = item;
    }
    return last == null ? Empty.VALUE : last;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr input = exprs[0];
    final SeqType st = input.seqType();
    if(st.zeroOrOne()) return input;

    final long size = input.size();
    // util:last(tail(E))  ->  util:last(E)
    if(TAIL.is(input) && size > 1)
      return cc.function(_UTIL_LAST, info, input.args());
    // util:last(util:init(E))  ->  util:item(E, size)
    if(_UTIL_INIT.is(input) && size > 0)
      return cc.function(_UTIL_ITEM, info, input.arg(0), Int.get(size));
    // util:last(reverse(E))  ->  head(E)
    if(REVERSE.is(input))
      return cc.function(HEAD, info, input.args());
    // util:last(replicate(E, count))  ->  util:last(E)
    if(REPLICATE.is(input)) {
      // static integer will always be greater than 1
      if(input.arg(1) instanceof Int) return cc.function(_UTIL_LAST, info, input.arg(0));
    }

    // util:last((1, 2))  ->  2
    // util:last((1, (2 to 3))  ->  util:last(2 to 3)
    if(input instanceof List) {
      final Expr[] args = input.args();
      final Expr last = args[args.length - 1];
      final SeqType stl = last.seqType();
      if(stl.one()) return last;
      if(stl.oneOrMore()) return cc.function(_UTIL_LAST, info, last);
    }
    // util:last(reverse(root)[test])  ->  head(root[test])
    if(input instanceof IterFilter) {
      final IterFilter filter = (IterFilter) input;
      final Expr root = cc.function(REVERSE, filter.info(), filter.root);
      return cc.function(HEAD, info, Filter.get(cc, filter.info(), root, filter.exprs));
    }

    exprType.assign(st.with(st.oneOrMore() ? Occ.EXACTLY_ONE : Occ.ZERO_OR_ONE));
    data(input.data());
    return embed(cc, false);
  }
}
