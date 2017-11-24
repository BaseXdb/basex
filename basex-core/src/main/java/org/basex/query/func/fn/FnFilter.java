package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.SeqType.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class FnFilter extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final FItem fun = checkArity(exprs[1], 1, qc);
    final Iter iter = qc.iter(exprs[0]);
    return new Iter() {
      @Override
      public Item next() throws QueryException {
        for(Item it; (it = iter.next()) != null;) {
          qc.checkStop();
          if(toBoolean(fun.invokeItem(qc, info, it))) return it;
        }
        return null;
      }
    };
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr ex = exprs[0];
    final SeqType st = ex.seqType();
    if(st.zero()) return ex;

    coerceFunc(1, cc, SeqType.BLN_O, st.type.seqType());

    exprType.assign(st.type, st.occ.union(Occ.ZERO));
    return this;
  }
}
