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
 * @author BaseX Team 2005-18, BSD License
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
          final Item it = qc.next(ir);
          if(it != null) return it;
          final Item item = iter.next();
          if(item == null) return null;
          ir = func.invokeValue(qc, info, item).iter();
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
    return vb.value();
  }

  @Override
  protected final Expr opt(final CompileContext cc) throws QueryException {
    final Expr expr1 = exprs[0];
    final SeqType st1 = expr1.seqType();
    if(st1.zero()) return expr1;

    coerceFunc(1, cc, SeqType.ITEM_ZM, st1.type.seqType());

    // assign type after coercion (expression might have changed)
    final boolean updating = this instanceof UpdateForEach;
    final Expr expr2 = exprs[1];
    final Type type2 = expr2.seqType().type;
    if(type2 instanceof FuncType && !updating) {
      final SeqType ft2 = ((FuncType) type2).declType;
      final boolean mayBeEmpty = st1.mayBeEmpty() || ft2.mayBeEmpty();
      final long size = ft2.zero() ? 0 : ft2.one() ? expr1.size() : -1;
      exprType.assign(ft2.type, mayBeEmpty ? Occ.ZERO_MORE : Occ.ONE_MORE, size);
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
