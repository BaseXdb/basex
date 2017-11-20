package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class FnForEach extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Iter iter = qc.iter(exprs[0]);
    final FItem fun = checkArity(exprs[1], 1, qc);

    return new Iter() {
      Iter ir2 = Empty.ITER;

      @Override
      public Item next() throws QueryException {
        do {
          final Item it = ir2.next();
          if(it != null) return it;
          final Item it2 = iter.next();
          if(it2 == null) return null;
          ir2 = fun.invokeValue(qc, info, it2).iter();
          qc.checkStop();
        } while(true);
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Iter iter = qc.iter(exprs[0]);
    final FItem fun = checkArity(exprs[1], 1, qc);

    final ValueBuilder vb = new ValueBuilder();
    for(Item it; (it = iter.next()) != null;) {
      qc.checkStop();
      vb.add(fun.invokeValue(qc, info, it));
    }
    return vb.value();
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr ex1 = exprs[0];
    final SeqType st1 = ex1.seqType();
    if(st1.zero()) return ex1;

    coerceFunc(1, cc, SeqType.ITEM_ZM, st1.type.seqType());

    // assign type after coercion (expression might have changed)
    final Expr ex2 = exprs[1];
    final Type t2 = ex2.seqType().type;
    if(t2 instanceof FuncType) exprType.assign(((FuncType) t2).declType.type);

    final long sz1 = ex1.size();
    if(allAreValues() && sz1 <= UNROLL_LIMIT) {
      // unroll the loop
      final Value seq = (Value) ex1;
      final Expr[] results = new Expr[(int) sz1];
      for(int i = 0; i < sz1; i++) {
        results[i] = new DynFuncCall(info, sc, ex2, seq.itemAt(i)).optimize(cc);
      }
      cc.info(QueryText.OPTUNROLL_X, this);
      return new List(info, results).optimize(cc);
    }

    return this;
  }
}
