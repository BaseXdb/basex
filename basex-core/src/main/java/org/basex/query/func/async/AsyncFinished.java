package org.basex.query.func.async;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class AsyncFinished extends StandardFunc {
  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] id = toToken(exprs[0], qc);
    return Bln.get(qc.context.queries.finished(Token.string(id), info));
  }
}
