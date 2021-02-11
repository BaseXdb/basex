package org.basex.query.func.math;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;

/**
 * Math function.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public abstract class MathFn extends StandardFunc {
  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    return optFirst();
  }
}
