package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.var.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FnFoldRight extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Value v = qc.value(exprs[0]);
    final FItem fun = checkArity(exprs[2], 2, qc);

    // evaluate start value lazily if it's passed straight through
    if(v.isEmpty()) return exprs[1].iter(qc);

    Value res = qc.value(exprs[1]);
    for(long i = v.size(); --i >= 0;) res = fun.invokeValue(qc, info, v.itemAt(i), res);
    return res.iter();
  }

  @Override
  protected Expr opt(final QueryContext qc, final VarScope scp) throws QueryException {
    if(allAreValues() && exprs[0].size() < FnForEach.UNROLL_LIMIT) {
      // unroll the loop
      qc.compInfo(QueryText.OPTUNROLL, this);
      final Value seq = (Value) exprs[0];
      Expr e = exprs[1];
      for(int i = (int) seq.size(); --i >= 0;) {
        e = new DynFuncCall(info, sc, false, exprs[2], seq.itemAt(i), e).optimize(qc, scp);
      }
      return e;
    }
    return this;
  }
}
