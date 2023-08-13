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
    final Expr lit = literal(qc);
    return lit != null ? lit.item(qc, info) : Empty.VALUE;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    // make sure that all functions are compiled
    cc.qc.functions.compileAll(cc);

    if(allAreValues(false)) {
      final Expr lit = literal(cc.qc);
      if(lit != null) return lit;
    }
    return this;
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    // locked resources cannot be detected statically
    return visitor.lock((String) null) && super.accept(visitor);
  }

  /**
   * Returns the requested function literal.
   * @param qc query context
   * @return literal or {@code null}
   * @throws QueryException query exception
   */
  private Expr literal(final QueryContext qc) throws QueryException {
    final QNm name = toQNm(toItem(arg(0), qc));
    final long arity = toLong(arg(1), qc);
    if(arity >= 0 && arity <= Integer.MAX_VALUE) {
      try {
        return Functions.literal(name, (int) arity, qc, sc, info, true);
      } catch(final QueryException ex) {
        Util.debug(ex);
      }
    }
    return null;
  }
}
