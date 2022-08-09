package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.value.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class FnHighest extends FnLowest {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return value(false, qc);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    return opt(false, cc);
  }
}
