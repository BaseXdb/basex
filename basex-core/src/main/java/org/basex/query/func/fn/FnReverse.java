package org.basex.query.func.fn;

import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class FnReverse extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    // optimization: reverse sequence
    final Iter input = exprs[0].iter(qc);

    // materialize value if number of results is unknown
    final long size = input.size();
    // no result: empty iterator
    if(size == 0) return Empty.ITER;
    // single result: iterator
    if(size == 1) return input;
    // value-based iterator
    if(input.valueIter()) return input.value(qc, null).reverse(qc).iter();

    // size is known: create iterator
    if(size > -1) return new Iter() {
      long c = size;

      @Override
      public Item next() throws QueryException {
        return --c >= 0 ? input.get(c) : null;
      }
      @Override
      public Item get(final long i) throws QueryException {
        return input.get(size - i - 1);
      }
      @Override
      public long size() {
        return size;
      }
    };

    // standard iterator
    final ValueBuilder vb = new ValueBuilder(qc);
    for(Item item; (item = qc.next(input)) != null;) {
      vb.addFront(item);
    }
    return vb.value(this).iter();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return exprs[0].value(qc).reverse(qc);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr input = exprs[0];
    if(input.seqType().zeroOrOne()) return input;
    if(input instanceof Value) return ((Value) input).reverse(cc.qc);

    // reverse(reverse(E))  ->  E
    if(REVERSE.is(input)) return input.arg(0);
    // reverse(tail(reverse(E))  ->  util:init(E)
    if(TAIL.is(input) && REVERSE.is(input.arg(0)))
      return cc.function(TRUNK, info, input.arg(0).args());
    // reverse(util:init(reverse(E))  ->  tail(E)
    if(TRUNK.is(input) && REVERSE.is(input.arg(0)))
      return cc.function(TAIL, info, input.arg(0).args());
    // reverse(replicate(ZOO, count))  ->  replicate(ZOO, count)
    if(REPLICATE.is(input) && !input.has(Flag.NDT))
      if(input.arg(0).seqType().zeroOrOne()) return input;

    // reverse((E1, E2))  ->  reverse(E2), reverse(E1)
    if(input instanceof List) {
      final Expr[] args = input.args();
      final int al = args.length;
      final ExprList list = new ExprList(al);
      for(int a = al - 1; a >= 0; a--) list.add(cc.function(REVERSE, info, args[a]));
      return List.get(cc, input.info(), list.finish());
    }
    // reverse(E[test])  ->  reverse(E)[test]
    if(input instanceof IterFilter) {
      final IterFilter filter = (IterFilter) input;
      if(filter.root.size() != -1) {
        return Filter.get(cc, info, cc.function(REVERSE, filter.info(), filter.root), filter.exprs);
      }
    }

    adoptType(input);
    return embed(cc, false);
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    return cc.simplify(this, mode.oneOf(Simplify.DISTINCT, Simplify.COUNT) ? exprs[0] : this, mode);
  }
}
