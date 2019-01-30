package org.basex.query.func.session;

import static org.basex.query.QueryError.*;

import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class SessionSet extends SessionFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item item = session(qc).set(toToken(exprs[0], qc), exprs[1].value(qc), qc);
    if(item != null) throw SESSION_SET_X.get(info, item);
    return null;
  }
}
