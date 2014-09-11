package org.basex.query.func.xquery;

import org.basex.query.*;
import org.basex.query.iter.*;

/**
 * XQuery functions.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class XQueryUpdate extends XQueryEval {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return eval(qc, toToken(exprs[0], qc), null, true);
  }
}
