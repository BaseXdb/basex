package org.basex.query.func.session;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
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
public final class SessionSet extends SessionFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final ASession session = session(qc, true);
    final byte[] name = toToken(exprs[0], qc);
    final Value value = exprs[1].value(qc), v = value.materialize(qc, n -> false, ii);
    if(v == null) throw SESSION_SET_X.get(info, value);

    session.set(name, v);
    return Empty.VALUE;
  }
}
