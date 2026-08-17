package org.basex.query.func.job;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.value.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class JobServices extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return qc.context.services.toXml().childIter().value(qc, this);
  }
}
