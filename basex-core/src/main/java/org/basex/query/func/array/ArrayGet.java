package org.basex.query.func.array;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class ArrayGet extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return toArray(exprs[0], qc).get(toAtomItem(exprs[1], qc), info);
  }

  @Override
  protected Expr opt(final QueryContext qc, final VarScope scp) {
    final SeqType st = exprs[0].seqType();
    if(st.type instanceof ArrayType) {
      final SeqType rt = ((ArrayType) st.type).type;
      if(rt != null) seqType = rt;
    }
    return this;
  }
}
