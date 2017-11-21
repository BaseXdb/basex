package org.basex.query.func.lazy;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class LazyCache extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value v = qc.value(exprs[0]);
    v.materialize(info);
    return v;
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    return adoptType(exprs[0]);
  }
}
