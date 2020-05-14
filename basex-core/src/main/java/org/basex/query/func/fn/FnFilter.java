package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-20, BSD License
 * @author Christian Gruen
 */
public final class FnFilter extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) {
    throw Util.notExpected();
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr items = exprs[0];
    final SeqType st = items.seqType();
    if(st.zero()) return items;

    // create filter expression
    final Expr pred = cc.get(items, () -> {
      Expr p = new ContextValue(info).optimize(cc);
      p = new DynFuncCall(info, sc, exprs[1], p).optimize(cc);
      p = new TypeCheck(sc, info, p, SeqType.BLN_O, true).optimize(cc);
      return p;
    });
    return Filter.get(info, items, cc.function(Function.BOOLEAN, info, pred)).optimize(cc);
  }
}
