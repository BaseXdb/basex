package org.basex.query.func.session;

import org.basex.query.*;
import org.basex.query.value.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-20, BSD License
 * @author Christian Gruen
 */
public final class SessionNames extends SessionFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return session(qc).names();
  }
}
