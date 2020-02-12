package org.basex.query.func.util;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-20, BSD License
 * @author Christian Gruen
 */
public final class UtilReplicate extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value value = exprs[0].value(qc);
    final long mult = toLong(exprs[1], qc);
    if(mult < 0) throw UTIL_NEGATIVE_X.get(info, mult);
    return SingletonSeq.get(value, mult);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr expr = exprs[0], mult = exprs[1];
    final SeqType st = expr.seqType();
    if(st.zero()) return expr;

    // pre-evaluate static multipliers
    long sz = -1, m = -1;
    if(mult instanceof Value) {
      m = toLong(mult, cc.qc);
      if(m == 0) return Empty.VALUE;
      if(m == 1) return expr;
      sz = expr.size();
      if(sz != -1) sz *= m;
    }

    // adopt sequence type
    exprType.assign(st.type, st.occ.union(m > 0 ? Occ.ONE_MORE : Occ.ZERO_MORE), sz);
    return this;
  }
}
