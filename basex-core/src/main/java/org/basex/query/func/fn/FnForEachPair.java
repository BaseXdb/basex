package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.func.update.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public class FnForEachPair extends StandardFunc {
  @Override
  public final Iter iter(final QueryContext qc) throws QueryException {
    final Iter input1 = arg(0).iter(qc), input2 = arg(1).iter(qc);
    final FItem action = toFunction(arg(2), 3, this instanceof UpdateForEachPair, qc);

    return new Iter() {
      final long size = action.funcType().declType.one() ?
        Math.min(input1.size(), input2.size()) : -1;
      final HofArgs args = new HofArgs(3, action);
      Iter iter = Empty.ITER;

      @Override
      public Item next() throws QueryException {
        while(true) {
          final Item item = iter.next();
          if(item != null) return item;
          final Item item1 = input1.next(), item2 = input2.next();
          if(item1 == null || item2 == null) return null;
          iter = invoke(action, args.set(0, item1).set(1, item2).inc(), qc).iter();
        }
      }

      @Override
      public Item get(final long i) throws QueryException {
        final Item item1 = input1.get(i), item2 = input2.get(i);
        return invoke(action, args.set(0, item1).set(1, item2), qc).item(qc, info);
      }

      @Override
      public long size() {
        return size;
      }
    };
  }

  @Override
  protected final Expr opt(final CompileContext cc) throws QueryException {
    final Expr input1 = arg(0), input2 = arg(1);
    final SeqType st1 = input1.seqType(), st2 = input2.seqType();
    if(st1.zero()) return input1;
    if(st2.zero()) return input2;

    arg(2, arg -> refineFunc(arg(2), cc, st1.with(Occ.EXACTLY_ONE), st2.with(Occ.EXACTLY_ONE),
        Types.INTEGER_O));

    // assign type after refinement
    final FuncType ft = arg(2).funcType();
    if(ft != null) {
      final SeqType declType = ft.declType;
      final boolean oneOrMore = st1.oneOrMore() && st2.oneOrMore() && declType.oneOrMore();
      final long size = declType.zero() ? 0 : declType.one() ?
        Math.min(input1.size(), input2.size()) : -1;
      exprType.assign(declType, oneOrMore ? Occ.ONE_OR_MORE : Occ.ZERO_OR_MORE, size);
    }
    return this;
  }
}
