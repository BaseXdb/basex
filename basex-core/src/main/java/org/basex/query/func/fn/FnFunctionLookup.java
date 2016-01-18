package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class FnFunctionLookup extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final QNm name = toQNm(exprs[0], qc, false);
    final long arity = toLong(exprs[1], qc);
    if(arity >= 0 && arity <= Long.MAX_VALUE) {
      try {
        final Expr lit = Functions.getLiteral(name, (int) arity, qc, sc, ii, true);
        if(lit != null) return lit.item(qc, ii);
      } catch(final QueryException ignore) { }
    }
    // function not found
    return null;
  }

  @Override
  protected Expr opt(final QueryContext qc, final VarScope scp) {
    for(final StaticFunc sf : qc.funcs.funcs()) sf.compile(qc);
    return this;
  }
}
