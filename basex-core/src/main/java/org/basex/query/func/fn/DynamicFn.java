package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;

/**
 * Function based on dynamic context properties (date, etc.).
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public abstract class DynamicFn extends StandardFunc {
  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    return cc.dynamic && allAreValues(true) ? value(cc.qc) : this;
  }
}
