package org.basex.query.func.xquery;

import org.basex.query.*;
import org.basex.query.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class XQueryUpdate extends XQueryEval {
  @Override
  protected ItemList eval(final QueryContext qc) throws QueryException {
    return eval(qc, toToken(exprs[0], qc), null, true);
  }
}
