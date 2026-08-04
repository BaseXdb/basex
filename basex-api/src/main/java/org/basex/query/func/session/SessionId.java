package org.basex.query.func.session;

import org.basex.query.*;
import org.basex.query.value.item.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class SessionId extends SessionFn {
  @Override
  protected Str item(final QueryContext qc) throws QueryException {
    final ASession session = session(qc, true);

    return session.id();
  }
}
