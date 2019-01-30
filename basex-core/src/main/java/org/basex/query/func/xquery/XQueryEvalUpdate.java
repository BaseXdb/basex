package org.basex.query.func.xquery;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class XQueryEvalUpdate extends XQueryEval {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return eval(true, qc).iter();
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return eval(true, qc).value();
  }
}
