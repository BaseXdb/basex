package org.basex.query.func.fn;

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
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public class FnFoot extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    // fast route if the size is known
    final Iter input = arg(0).iter(qc);
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
    final Expr input = arg(0);
    final SeqType st = input.seqType();
    if(st.zeroOrOne()) return input;

    final long size = input.size();
    // foot(tail(E))  ->  foot(E)
    if(TAIL.is(input) && size > 1)
      return cc.function(FOOT, info, input.args());
    // foot(trunk(E))  ->  items-at(E, size)
    if(TRUNK.is(input) && size > 0)
      return cc.function(ITEMS_AT, info, input.arg(0), Int.get(size));
    // foot(reverse(E))  ->  head(E)
    if(REVERSE.is(input))
      return cc.function(HEAD, info, input.args());
    // foot(replicate(E, count))  ->  foot(E)
    if(REPLICATE.is(input)) {
      // static integer will always be greater than 1
      if(input.arg(1) instanceof Int) return cc.function(FOOT, info, input.arg(0));
    }

    // foot((1, 2))  ->  2
    // foot((1, (2 to 3))  ->  foot(2 to 3)
    if(input instanceof List) {
      final Expr[] args = input.args();
      final Expr last = args[args.length - 1];
      final SeqType stl = last.seqType();
      if(stl.one()) return last;
      if(stl.oneOrMore()) return cc.function(FOOT, info, last);
    }
    // foot(reverse(root)[test])  ->  head(root[test])
    if(input instanceof IterFilter) {
      final IterFilter filter = (IterFilter) input;
      final Expr root = cc.function(REVERSE, filter.info(), filter.root);
      return cc.function(HEAD, info, Filter.get(cc, filter.info(), root, filter.exprs));
    }

    exprType.assign(st.with(st.oneOrMore() ? Occ.EXACTLY_ONE : Occ.ZERO_OR_ONE)).data(input);
    return embed(cc, false);
  }
}
