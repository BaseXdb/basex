package org.basex.query.func.jobs;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class JobsResult extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final String id = Token.string(toToken(exprs[0], qc));
    return qc.context.queries.result(id, info);
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }
}
