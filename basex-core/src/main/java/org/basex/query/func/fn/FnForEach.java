package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.func.update.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public class FnForEach extends StandardFunc {
  @Override
  public final Iter iter(final QueryContext qc) throws QueryException {
    final Iter iter = exprs[0].iter(qc);
    final FItem func = checkArity(exprs[1], 1, this instanceof UpdateForEach, qc);

    return new Iter() {
      Iter ir = Empty.ITER;

      @Override
      public Item next() throws QueryException {
        do {
          final Item item1 = qc.next(ir);
          if(item1 != null) return item1;
          final Item item2 = iter.next();
          if(item2 == null) return null;
          ir = func.invokeValue(qc, info, item2).iter();
        } while(true);
      }
    };
  }

  @Override
  public final Value value(final QueryContext qc) throws QueryException {
    final Iter iter = exprs[0].iter(qc);
    final FItem func = checkArity(exprs[1], 1, this instanceof UpdateForEach, qc);

    final ValueBuilder vb = new ValueBuilder(qc);
    for(Item item; (item = qc.next(iter)) != null;) vb.add(func.invokeValue(qc, info, item));
    return vb.value(this);
  }

  @Override
  protected final Expr opt(final CompileContext cc) throws QueryException {
    final Expr expr1 = exprs[0];
    final SeqType st1 = expr1.seqType();
    if(st1.zero()) return expr1;

    exprs[1] = coerceFunc(exprs[1], cc, SeqType.ITEM_ZM, st1.with(Occ.ONE));

    // assign type after coercion (expression might have changed)
    final boolean updating = this instanceof UpdateForEach;
    final Expr expr2 = exprs[1];
    final FuncType ft = expr2.funcType();
    if(ft != null && !updating) {
      final SeqType declType = ft.declType;
      final boolean oneOrMore = st1.oneOrMore() && declType.oneOrMore();
      final long size = declType.zero() ? 0 : declType.one() ? expr1.size() : -1;
      exprType.assign(declType.type, oneOrMore ? Occ.ONE_MORE : Occ.ZERO_MORE, size);
    }

    final long size1 = expr1.size();
    if(allAreValues(false) && size1 <= UNROLL_LIMIT) {
      // unroll the loop
      final boolean ndt = expr2.has(Flag.NDT);
      final Value seq = (Value) expr1;
      final Expr[] results = new Expr[(int) size1];
      for(int i = 0; i < size1; i++) {
        results[i] = new DynFuncCall(info, sc, updating, ndt, expr2, seq.itemAt(i)).optimize(cc);
      }
      cc.info(QueryText.OPTUNROLL_X, this);
      return new List(info, results).optimize(cc);
    }

    return this;
  }
}
