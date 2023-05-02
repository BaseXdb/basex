package org.basex.query.func.util;

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
public final class UtilOr extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    // implementation for dynamic function lookup
    return build().value(qc);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    return build().optimize(cc);
  }

  /**
   * Creates a new {@link Otherwise} expression.
   * @return new otherwise expression
   */
  private Otherwise build() {
    return new Otherwise(info, exprs);
  }
}
