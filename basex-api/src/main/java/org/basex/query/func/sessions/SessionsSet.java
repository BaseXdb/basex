package org.basex.query.func.sessions;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.func.session.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class SessionsSet extends SessionsFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final ASession session = session(qc);
    final byte[] name = toToken(exprs[1], qc);
    final Value value = exprs[2].value(qc), v = value.materialize(n -> false, ii, qc);
    if(v == null) throw SESSIONS_SET_X.get(info, value);

    session.set(name, v);
    return Empty.VALUE;
  }
}
