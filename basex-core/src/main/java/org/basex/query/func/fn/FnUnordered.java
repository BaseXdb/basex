package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.var.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class FnUnordered extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return exprs[0].iter(qc);
  }

  @Override
  protected Expr opt(final QueryContext qc, final VarScope scp) {
    return exprs[0];
  }
}
