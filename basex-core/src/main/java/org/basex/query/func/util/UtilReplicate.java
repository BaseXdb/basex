package org.basex.query.func.util;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.SeqType.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class UtilReplicate extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value value = qc.value(exprs[0]);
    final long mult = toLong(exprs[1], qc);
    if(mult < 0) throw UTIL_NEGATIVE_X.get(info, mult);
    return SingletonSeq.get(value, mult);
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr ex = exprs[0], mult = exprs[1];

    // pre-evaluate static multipliers
    final long m = mult instanceof Value ? toLong(mult, cc.qc) : -1;
    if(m == 0 || ex == Empty.SEQ) return Empty.SEQ;
    if(m == 1) return ex;

    // adopt sequence type
    final SeqType st = ex.seqType();
    exprType.assign(st.type, st.occ.union(m > 1 ? Occ.ONE_MORE : Occ.ZERO_MORE));
    return this;
  }
}
