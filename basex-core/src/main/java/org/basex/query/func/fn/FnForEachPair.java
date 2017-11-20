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
public final class FnForEachPair extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Iter ir1 = qc.iter(exprs[0]), ir2 = qc.iter(exprs[1]);
    final FItem fun = checkArity(exprs[2], 2, qc);

    return new Iter() {
      Iter iter = Empty.ITER;

      @Override
      public Item next() throws QueryException {
        do {
          final Item it = iter.next();
          if(it != null) return it;
          final Item it1 = ir1.next(), it2 = ir2.next();
          if(it1 == null || it2 == null) return null;
          iter = fun.invokeValue(qc, info, it1, it2).iter();
          qc.checkStop();
        } while(true);
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Iter ir1 = qc.iter(exprs[0]), ir2 = qc.iter(exprs[1]);
    final FItem fun = checkArity(exprs[2], 2, qc);

    final ValueBuilder vb = new ValueBuilder();
    for(Item it1, it2; (it1 = ir1.next()) != null && (it2 = ir2.next()) != null;) {
      qc.checkStop();
      vb.add(fun.invokeValue(qc, info, it1, it2));
    }
    return vb.value();
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr ex1 = exprs[0], ex2 = exprs[1];
    final SeqType st1 = ex1.seqType(), st2 = ex2.seqType();
    if(st1.zero()) return ex1;
    if(st2.zero()) return ex2;

    coerceFunc(2, cc, SeqType.ITEM_ZM, st1.type.seqType(), st2.type.seqType());

    // assign type after coercion (expression might have changed)
    final Type t3 = exprs[2].seqType().type;
    if(t3 instanceof FuncType) exprType.assign(((FuncType) t3).declType.type);

    return this;
  }
}
