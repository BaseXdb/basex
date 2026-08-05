package org.basex.query.func.sessions;

import org.basex.query.*;
import org.basex.query.func.session.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class SessionsDelete extends SessionsFn {
  @Override
  protected Item item(final QueryContext qc) throws QueryException {
    final ASession session = session(qc);
    final String key = toString(arg(1), qc);

    session.delete(key);
    return Empty.VALUE;
  }
}
