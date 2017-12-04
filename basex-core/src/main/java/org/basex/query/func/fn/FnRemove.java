package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class FnRemove extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final long p = toLong(exprs[1], qc) - 1;
    final Iter iter = exprs[0].iter(qc);
    final long is = iter.size();
    return p < 0 || is != -1 && p > is ? iter : new Iter() {
      long c;
      @Override
      public Item next() throws QueryException {
        return c++ != p || iter.next() != null ? qc.next(iter) : null;
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value val = exprs[0].value(qc);
    final long pos = toLong(exprs[1], qc) - 1, n = val.size();
    if(pos < 0 || pos >= n) return val;
    // remove first or last item: create sub sequence
    if(pos == 0 || pos + 1 == n) return val.subSequence(pos == 0 ? 1 : 0, n - 1, qc);
    return ((Seq) val).remove(pos, qc);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    // ignore standard limitation for large values
    if(allAreValues(false)) return value(cc.qc);

    final Expr ex = exprs[0];
    final SeqType st = ex.seqType();
    if(st.zero()) return ex;
    exprType.assign(st.type, st.occ.union(Occ.ZERO));
    return this;
  }
}
