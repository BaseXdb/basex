package org.basex.query.func.lazy;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class LazyCache extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value value = exprs[0].value(qc);
    final boolean lazy = exprs.length > 1 && toBoolean(exprs[1], qc);
    value.cache(lazy, info);
    return value;
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    return adoptType(exprs[0]);
  }

  @Override
  public boolean ddo() {
    return exprs[0].ddo();
  }
}
