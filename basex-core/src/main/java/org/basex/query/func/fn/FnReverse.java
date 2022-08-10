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
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
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
    final Value value = input.iterValue();
    if(value != null) return value.reverse(qc).iter();

    // fast route if the size is known
    if(size > -1) return new Iter() {
      long c = size;

      @Override
      public Item next() throws QueryException {
        qc.checkStop();
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
    for(Item item; (item = qc.next(input)) != null;) vb.addFront(item);
    return vb.value(this).iter();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return exprs[0].value(qc).reverse(qc);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr input = exprs[0];
    // zero/single items or singleton sequence: return argument
    if(input.seqType().zeroOrOne() || input instanceof SingletonSeq &&
        ((SingletonSeq) input).singleItem()) return input;
    // reverse sequence
    if(input instanceof RangeSeq) return ((RangeSeq) input).reverse(cc.qc);

    // reverse(reverse(E))  ->  E
    if(REVERSE.is(input)) return input.arg(0);
    // reverse(tail(reverse(E))  ->  util:init(E)
    if(TAIL.is(input) && REVERSE.is(input.arg(0)))
      return cc.function(_UTIL_INIT, info, input.arg(0).args());
    // reverse(util:init(reverse(E))  ->  tail(E)
    if(_UTIL_INIT.is(input) && REVERSE.is(input.arg(0)))
      return cc.function(TAIL, info, input.arg(0).args());
    // reverse(replicate(ITEM, COUNT))  ->  replicate(ITEM, COUNT)
    if(REPLICATE.is(input) && !input.has(Flag.NDT))
      if(input.arg(0).seqType().zeroOrOne()) return input;

    // rewrite list
    if(input instanceof List) {
      final Expr[] args = input.args();
      if(((Checks<Expr>) ex -> ex instanceof Value || ex.seqType().zeroOrOne()).all(args)) {
        final int al = args.length;
        final ExprList list = new ExprList(al);
        for(int a = al - 1; a >= 0; a--) {
          list.add(args[a] instanceof Value ? ((Value) args[a]).reverse(cc.qc) : args[a]);
        }
        return List.get(cc, input.info(), list.finish());
      }
    }
    final Expr embedded = embed(cc, false);
    if(embedded != null) return embedded;

    return adoptType(input);
  }

  @Override
  public Expr simplifyFor(final Simplify mode, final CompileContext cc) throws QueryException {
    return mode.oneOf(Simplify.DISTINCT, Simplify.COUNT)
        ? cc.simplify(this, exprs[0]).simplifyFor(mode, cc)
        : super.simplifyFor(mode, cc);
  }
}
