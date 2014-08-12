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
  protected Expr opt(final QueryContext qc, final VarScope scp) {
    // simplify, e.g.: if(boolean(A)) -> if(A)
    final Expr e = exprs[0];
    return e.seqType().eq(SeqType.BLN) ? e : this;
  }

  @Override
  public Expr compEbv(final QueryContext qc) {
    // (test)[boolean(A)] -> (test)[A]
    final Expr e = exprs[0];
    if(!e.seqType().mayBeNumber()) {
      qc.compInfo(QueryText.OPTWRITE, this);
      return e;
    }
    return this;
  }
}
