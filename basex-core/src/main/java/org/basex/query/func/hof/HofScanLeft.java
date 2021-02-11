package org.basex.query.func.hof;

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
 * @author BaseX Team 2005-21, BSD License
 * @author Leo Woerteler
 */
public final class HofScanLeft extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Iter outer = exprs[0].iter(qc);
    final FItem func = checkArity(exprs[2], 2, qc);

    return new Iter() {
      private Value acc = exprs[1].value(qc);
      private Iter inner = acc.iter();

      @Override
      public Item next() throws QueryException {
        while(true) {
          final Item in = qc.next(inner);
          if(in != null) return in;
          final Item out = outer.next();
          if(out == null) return null;

          acc = func.invoke(qc, info, acc, out);
          inner = acc.iter();
        }
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return iter(qc).value(qc, this);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr expr1 = exprs[0], expr2 = exprs[1];
    if(expr1 == Empty.VALUE) return expr2;

    exprType.assign(expr1.seqType().union(Occ.ZERO));
    data(expr1.data());
    return this;
  }
}
