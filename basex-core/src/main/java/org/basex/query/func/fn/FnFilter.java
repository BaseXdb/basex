package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class FnFilter extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final FItem func = checkArity(exprs[1], 1, qc);
    final Iter iter = exprs[0].iter(qc);
    return new Iter() {
      @Override
      public Item next() throws QueryException {
        for(Item item; (item = qc.next(iter)) != null;) {
          if(toBoolean(func.invokeItem(qc, info, item))) return item;
        }
        return null;
      }
    };
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr expr = exprs[0];
    final SeqType st = expr.seqType();
    if(st.zero()) return expr;

    exprs[1] = coerceFunc(exprs[1], cc, SeqType.BLN_O, st.with(Occ.ONE));
    exprType.assign(st.type, st.occ.union(Occ.ZERO));
    return this;
  }
}
