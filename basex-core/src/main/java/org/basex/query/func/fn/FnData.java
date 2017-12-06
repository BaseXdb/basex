package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class FnData extends ContextFn {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return ctxArg(0, qc).atomIter(qc, info);
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return ctxArg(0, qc).atomValue(qc, info);
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return ctxArg(0, qc).atomItem(qc, info);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Value value = cc.qc.focus.value;
    final Expr expr = exprs.length > 0 ? exprs[0] : value != null ? value : this;
    if(expr != this) {
      final SeqType st = expr.seqType();
      final Type type = st.atomicType();
      if(type != null) exprType.assign(type, st.occ, expr.size());
    }
    return this;
  }
}
