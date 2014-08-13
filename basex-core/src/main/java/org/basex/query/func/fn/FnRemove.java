package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.SeqType.*;
import org.basex.query.var.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FnRemove extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final long p = toLong(exprs[1], qc) - 1;
    final Iter ir = exprs[0].iter(qc);
    final long is = ir.size();
    return p < 0 || is != -1 && p > is ? ir : new Iter() {
      long c;
      @Override
      public Item next() throws QueryException {
        return c++ != p || ir.next() != null ? ir.next() : null;
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value val = qc.value(exprs[0]);
    final long p = toLong(exprs[1], qc) - 1, vs = val.size() - 1;
    if(p < 0 || p > vs) return val;
    if(p == 0 || p == vs) return SubSeq.get(val, p == 0 ? 1 : 0, vs);
    final ValueBuilder vb = new ValueBuilder((int) vs);
    for(int v = 0; v <= vs; v++) if(v != p) vb.add(val.itemAt(v));
    return vb.value();
  }

  @Override
  protected Expr opt(final QueryContext qc, final VarScope scp) throws QueryException {
    final SeqType st = exprs[0].seqType();
    seqType = SeqType.get(st.type, st.zeroOrOne() ? Occ.ZERO_ONE : Occ.ZERO_MORE);
    return this;
  }
}
