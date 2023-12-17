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
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class FnFunctionLookup extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Expr expr = item(qc);
    return expr != null ? expr.item(qc, info) : Empty.VALUE;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    // make sure that all functions are compiled
    cc.qc.functions.compileAll(cc);

    if(allAreValues(false)) {
      final Expr expr = item(cc.qc);
      if(expr != null) return expr;
    }
    return this;
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    // locked resources cannot be detected statically
    return visitor.lock((String) null) && super.accept(visitor);
  }

  /**
   * Returns the requested function item.
   * @param qc query context
   * @return literal or {@code null}
   * @throws QueryException query exception
   */
  private Expr item(final QueryContext qc) throws QueryException {
    final QNm name = toQNm(toItem(arg(0), qc));
    final long arity = toLong(arg(1), qc);
    if(arity >= 0 && arity <= Integer.MAX_VALUE) {
      try {
        return Functions.item(name, (int) arity, true, sc, info, qc);
      } catch(final QueryException ex) {
        Util.debug(ex);
      }
    }
    return null;
  }
}
