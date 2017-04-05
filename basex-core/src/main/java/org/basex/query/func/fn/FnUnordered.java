package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class FnUnordered extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return qc.iter(exprs[0]);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    return exprs[0];
  }
}
