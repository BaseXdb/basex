package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.var.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class FnForEach extends StandardFunc {
  /** Minimum size of a loop that should not be unrolled. */
  public static final int UNROLL_LIMIT = 10;

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final FItem f = checkArity(exprs[1], 1, qc);
    final Iter ir = exprs[0].iter(qc);
    return new Iter() {
      Iter ir2 = Empty.ITER;

      @Override
      public Item next() throws QueryException {
        do {
          final Item it = ir2.next();
          if(it != null) return it;
          final Item it2 = ir.next();
          if(it2 == null) return null;
          ir2 = f.invokeValue(qc, info, it2).iter();
        } while(true);
      }
    };
  }

  @Override
  protected Expr opt(final QueryContext qc, final VarScope scp) throws QueryException {
    if(allAreValues() && exprs[0].size() < UNROLL_LIMIT) {
      // unroll the loop
      qc.compInfo(QueryText.OPTUNROLL, this);
      final Value seq = (Value) exprs[0];
      final int len = (int) seq.size();

      // fn:for-each(...)
      final Expr[] results = new Expr[len];
      for(int i = 0; i < len; i++) {
        results[i] = new DynFuncCall(info, sc, false, exprs[1], seq.itemAt(i)).optimize(qc, scp);
      }
      return new List(info, results).optimize(qc, scp);
    }
    return this;
  }
}
