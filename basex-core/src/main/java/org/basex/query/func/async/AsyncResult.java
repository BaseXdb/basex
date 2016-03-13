package org.basex.query.func.async;

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
public final class AsyncResult extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final byte[] id = toToken(exprs[0], qc);
    return qc.context.queries.result(Token.string(id), info);
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }
}
