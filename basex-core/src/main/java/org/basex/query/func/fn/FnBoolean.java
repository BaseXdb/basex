package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FnBoolean extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return Bln.get(exprs[0].ebv(qc, info).bool(info));
  }

  @Override
  protected Expr opt(final QueryContext qc, final VarScope scp) throws QueryException {
    final Expr e = exprs[0].optimizeEbv(qc, scp);
    exprs[0] = e;
    // simplify, e.g.: if(boolean(A)) -> if(A)
    return e.seqType().eq(SeqType.BLN) ? e : this;
  }

  @Override
  public Expr optimizeEbv(final QueryContext qc, final VarScope scp) {
    // expr[boolean(A)] -> expr[A]
    final Expr e = exprs[0];
    if(!e.seqType().mayBeNumber()) {
      qc.compInfo(QueryText.OPTWRITE, this);
      return e;
    }
    return this;
  }
}
