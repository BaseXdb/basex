package org.basex.query.func.fn;

import static org.basex.query.func.Function.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.expr.List;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnFoot extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    // value-based input: return last item
    final Expr input = arg(0);
    final Value value = input.eagerValue(qc);
    if(value != null) {
      final long size = value.size();
      return size > 0 ? value.itemAt(size - 1) : Empty.VALUE;
    }

    // fast route if the size is known
    final Iter iter = input.iter(qc);
    final long size = iter.size();
    if(size >= 0) return size > 0 ? iter.get(size - 1) : Empty.VALUE;

    // loop through all items
    Item last = null;
    for(Item item; (item = qc.next(iter)) != null;) {
      last = item;
    }
    return last == null ? Empty.VALUE : last;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr input = arg(0);
    final SeqType st = input.seqType();
    // foot($x) → $x
    if(st.zeroOrOne()) return input;

    final long size = input.size();
    // foot(tail(E)) → foot(E)
    if(TAIL.is(input) && size > 1)
      return cc.function(FOOT, info, input.args());
    // foot(trunk(E)) → items-at(E, size)
    if(TRUNK.is(input) && size > 0)
      return cc.function(ITEMS_AT, info, input.arg(0), Itr.get(size));
    // foot(reverse(E)) → head(E)
    if(REVERSE.is(input))
      return cc.function(HEAD, info, input.args());
    // foot(sort(E)) → foot(highest(E))
    if(input instanceof final FnSortBy sort) {
      final Expr extreme = sort.extreme(false, cc);
      if(extreme != null) return cc.function(FOOT, info, extreme);
    }
    // foot(replicate(E, count)) → foot(E) (static integer will always be greater than 1)
    if(REPLICATE.is(input) && input.arg(1) instanceof Itr)
      return cc.function(FOOT, info, input.arg(0));

    // rewrite list to its arguments or to elvis operator
    if(input instanceof List) {
      final Expr[] args = input.args();
      final int al = args.length;
      final Expr last = args[al - 1];
      final SeqType stl = last.seqType();
      // foot((1, 2)) → 2
      if(stl.one()) return last;
      // foot((1, (2 to 3))) → foot(2 to 3)
      if(stl.oneOrMore()) return cc.function(FOOT, info, last);
      // the elvis operator evaluates the operands in reverse order
      if(stl.zeroOrOne() && (al == 2 || args[al - 2].seqType().occ != Occ.ZERO_OR_ONE) &&
          !input.has(Flag.NDT)) {
        // foot((1, $a[.]))        → $a[.] otherwise 1
        // foot((1, $a[.], $b[.])) → (will not be rewritten)
        final Expr dflt = List.get(cc, info, Arrays.copyOf(args, al - 1));
        return new Otherwise(info, last, cc.function(FOOT, info, dflt)).optimize(cc);
      }
    }
    // foot(root[test]) → head(reverse(root)[test])
    if(input instanceof final IterFilter filter) {
      final Expr root = cc.function(REVERSE, filter.info(), filter.root);
      return cc.function(HEAD, info, Filter.get(cc, filter.info(), root, filter.exprs));
    }

    exprType.assign(st.with(st.oneOrMore() ? Occ.EXACTLY_ONE : Occ.ZERO_OR_ONE)).data(input);
    return embed(cc, false);
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    // exists(foot(E)) → exists(E)
    final Expr expr = mode == Simplify.EXISTENCE && !arg(0).has(Flag.NDT) ? arg(0) : this;
    return cc.simplify(this, expr, mode);
  }
}
