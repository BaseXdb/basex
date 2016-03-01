package org.basex.query.func.async;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class AsyncIds extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return StrSeq.get(qc.context.queries.ids());
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return value(qc).iter();
  }
}
