package org.basex.query.func.session;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class SessionClose extends SessionFn {
  @Override
  protected Item item(final QueryContext qc) throws QueryException {
    final ASession session = session(qc, false);

    if(session != null) session.close();
    return Empty.VALUE;
  }
}
