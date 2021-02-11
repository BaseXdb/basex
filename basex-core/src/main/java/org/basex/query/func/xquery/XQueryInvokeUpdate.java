package org.basex.query.func.xquery;

import org.basex.query.*;
import org.basex.query.value.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class XQueryInvokeUpdate extends XQueryEval {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return eval(toQuery(toToken(exprs[0], qc), qc), true, qc);
  }
}
