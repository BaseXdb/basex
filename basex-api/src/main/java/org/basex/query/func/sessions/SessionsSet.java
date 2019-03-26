package org.basex.query.func.sessions;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class SessionsSet extends SessionsFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item item = session(qc).set(toToken(exprs[1], qc), exprs[2].value(qc), qc);
    if(item != null) throw SESSIONS_SET_X.get(info, item);
    return Empty.VALUE;
  }
}
