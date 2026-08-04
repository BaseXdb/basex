package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnFunctionLookup extends StandardFunc {
  @Override
  protected Item item(final QueryContext qc) throws QueryException {
    final Expr expr = lookup(qc);
    return expr != null ? expr.item(qc, info) : Empty.VALUE;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    // make sure that all functions are compiled
    if(!cc.dynamic) cc.qc.functions.compileAll(cc);

    if(values(false, cc)) {
      final Expr expr = lookup(cc.qc);
      if(expr != null) return expr;
    }
    return this;
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    // locked resources cannot be detected statically
    return visitor.lock((String) null, false) && super.accept(visitor);
  }

  /**
   * Returns the requested function item.
   * @param qc query context
   * @return literal or {@code null}
   * @throws QueryException query exception
   */
  private Expr lookup(final QueryContext qc) throws QueryException {
    final QNm name = toQNm(toAtomItem(arg(0), qc));
    final long arity = toLong(arg(1), qc);
    if(arity >= 0 && arity <= Integer.MAX_VALUE) {
      try {
        return Functions.item(name, (int) arity, true, info, qc);
      } catch(final QueryException ignore) {
        // function is not available
      }
    }
    return null;
  }
}
