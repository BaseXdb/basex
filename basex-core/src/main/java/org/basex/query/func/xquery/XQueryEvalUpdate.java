package org.basex.query.func.xquery;

import org.basex.query.*;
import org.basex.query.value.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class XQueryEvalUpdate extends XQueryEval {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return eval(toContent(arg(0), qc), true, qc);
  }
}
