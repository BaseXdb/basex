package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class FnConcat extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    // implementation for dynamic function lookup
    return build().value(qc);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    return build().optimize(cc);
  }

  /**
   * Creates a new {@link Concat} expression.
   * @return new otherwise expression
   */
  private Concat build() {
    return new Concat(info, exprs);
  }
}
