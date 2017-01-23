package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class FnBoolean extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return Bln.get(exprs[0].ebv(qc, info).bool(info));
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr e = exprs[0].optimizeEbv(cc);
    exprs[0] = e;
    // simplify, e.g.: boolean(true())) -> true()
    return e.seqType().eq(SeqType.BLN) ? e : this;
  }

  @Override
  public Expr optimizeEbv(final CompileContext cc) {
    // if A is not numeric: expr[boolean(A)] -> expr[A]
    final Expr e = exprs[0];
    if(!e.seqType().mayBeNumber()) {
      cc.info(QueryText.OPTREWRITE_X, this);
      return e;
    }
    return this;
  }
}
