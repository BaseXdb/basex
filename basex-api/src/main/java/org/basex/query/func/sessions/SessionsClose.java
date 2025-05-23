package org.basex.query.func.sessions;

import org.basex.query.*;
import org.basex.query.func.session.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class SessionsClose extends SessionsFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final ASession session = session(qc);

    session.close();
    return Empty.VALUE;
  }
}
