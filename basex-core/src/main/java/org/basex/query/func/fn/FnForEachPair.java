package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.func.update.*;
import org.basex.query.iter.*;
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
public class FnForEachPair extends StandardFunc {
  @Override
  public final Iter iter(final QueryContext qc) throws QueryException {
    final Iter iter1 = exprs[0].iter(qc), iter2 = exprs[1].iter(qc);
    final FItem func = checkArity(exprs[2], 2, this instanceof UpdateForEachPair, qc);

    return new Iter() {
      Iter iter = Empty.ITER;

      @Override
      public Item next() throws QueryException {
        do {
          final Item item = qc.next(iter);
          if(item != null) return item;
          final Item item1 = iter1.next(), item2 = iter2.next();
          if(item1 == null || item2 == null) return null;
          iter = func.invokeValue(qc, info, item1, item2).iter();
        } while(true);
      }
    };
  }

  @Override
  public final Value value(final QueryContext qc) throws QueryException {
    final Iter iter1 = exprs[0].iter(qc), iter2 = exprs[1].iter(qc);
    final FItem func = checkArity(exprs[2], 2, this instanceof UpdateForEachPair, qc);

    final ValueBuilder vb = new ValueBuilder(qc);
    for(Item item1, item2; (item1 = iter1.next()) != null && (item2 = iter2.next()) != null;) {
      vb.add(func.invokeValue(qc, info, item1, item2));
    }
    return vb.value();
  }

  @Override
  protected final Expr opt(final CompileContext cc) throws QueryException {
    final Expr expr1 = exprs[0], expr2 = exprs[1];
    final SeqType st1 = expr1.seqType(), st2 = expr2.seqType();
    if(st1.zero()) return expr1;
    if(st2.zero()) return expr2;
    coerceFunc(2, cc, SeqType.ITEM_ZM, st1.type.seqType(), st2.type.seqType());

    // assign type after coercion (expression might have changed)
    final boolean updating = this instanceof UpdateForEachPair;
    final Type type3 = exprs[2].seqType().type;
    if(type3 instanceof FuncType && !updating) exprType.assign(((FuncType) type3).declType.type);

    return this;
  }
}
