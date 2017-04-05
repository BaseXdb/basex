package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.SeqType.Occ;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class FnTail extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Iter iter = qc.iter(exprs[0]);
    return iter.size() == 1 || iter.next() == null ? Empty.ITER : iter;
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value val = qc.value(exprs[0]);
    final long vs = val.size();
    return vs < 2 ? Empty.SEQ : val.subSeq(1, vs - 1);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr e = exprs[0];
    final SeqType st = e.seqType();
    if(st.zeroOrOne()) return e;
    seqType = st.withOcc(Occ.ZERO_MORE);
    return this;
  }
}
