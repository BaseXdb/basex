package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnMax extends FnMin {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return minmax(false, qc);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    return opt(false, cc);
  }
}
