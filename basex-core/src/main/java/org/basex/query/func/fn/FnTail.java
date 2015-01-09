package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.value.type.SeqType.Occ;
import org.basex.query.var.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class FnTail extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Expr e = exprs[0];
    if(e.seqType().zeroOrOne()) return Empty.ITER;

    final Iter ir = e.iter(qc);
    if(ir instanceof ValueIter) {
      final Value val = ir.value();
      return SubSeq.get(val, 1, val.size() - 1).iter();
    }
    if(ir.next() == null) return Empty.ITER;

    return new Iter() {
      @Override
      public Item next() throws QueryException {
        return ir.next();
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value val = qc.value(exprs[0]);
    return SubSeq.get(val, 1, val.size() - 1);
  }

  @Override
  protected Expr opt(final QueryContext qc, final VarScope scp) {
    final SeqType st = exprs[0].seqType();
    seqType = st.zeroOrOne() ? SeqType.EMP : SeqType.get(st.type, Occ.ZERO_MORE);
    return this;
  }
}
