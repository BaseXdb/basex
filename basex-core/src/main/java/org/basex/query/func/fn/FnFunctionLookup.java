package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FnFunctionLookup extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final QNm name = toQNm(exprs[0], qc, false);
    final long arity = toLong(exprs[1], qc);
    if(arity >= 0 && arity <= Integer.MAX_VALUE) {
      try {
        final Expr lit = Functions.getLiteral(name, (int) arity, qc, sc, info, true);
        if(lit != null) return lit.item(qc, info);
      } catch(final QueryException ignore) { }
    }
    // function not found
    return Empty.VALUE;
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    // make sure that all functions are compiled
    cc.qc.funcs.compileAll(cc);
    return this;
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    // locked resources cannot be detected statically
    return visitor.lock(null, false) && super.accept(visitor);
  }
}
