package org.basex.query.func.xquery;

import org.basex.query.*;
import org.basex.query.iter.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class XQueryUpdate extends XQueryEval {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return eval(qc, toToken(exprs[0], qc), null, true);
  }
}
