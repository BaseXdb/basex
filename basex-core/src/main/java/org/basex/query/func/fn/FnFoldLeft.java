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
public final class FnFoldLeft extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Iter ir = exprs[0].iter(qc);
    final FItem fun = checkArity(exprs[2], 2, qc);

    // don't convert to a value if not necessary
    Item it = ir.next();
    if(it == null) return exprs[1].iter(qc);

    Value res = qc.value(exprs[1]);
    do res = fun.invokeValue(qc, info, res, it);
    while((it = ir.next()) != null);
    return res.iter();
  }

  @Override
  protected Expr opt(final QueryContext qc, final VarScope scp) throws QueryException {
    if(allAreValues() && exprs[0].size() < FnForEach.UNROLL_LIMIT) {
      // unroll the loop
      qc.compInfo(QueryText.OPTUNROLL, this);
      final Value seq = (Value) exprs[0];
      Expr e = exprs[1];
      for(final Item it : seq) {
        e = new DynFuncCall(info, sc, false, exprs[2], e, it).optimize(qc, scp);
      }
      return e;
    }
    return this;
  }
}
