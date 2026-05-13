package org.basex.query.func.lazy;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.value.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class LazyCache extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value input = arg(0).value(qc);
    final boolean lazy = toBooleanOrFalse(arg(1), qc);
    input.cache(lazy, info);
    return input;
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    return adoptType(arg(0));
  }

  @Override
  public boolean ddo() {
    return arg(0).ddo();
  }
}
