package org.basex.query.func.ws;

import static org.basex.query.QueryError.*;

import org.basex.http.ws.*;
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
public final class WsSet extends WsFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final WebSocket client = client(qc);
    final String key = toString(exprs[1], qc);
    final Value value = exprs[2].value(qc), v = value.materialize(qc, n -> false, ii);
    if(v == null) throw WS_SET_X.get(info, value);

    client.atts.put(key, v);
    return Empty.VALUE;
  }
}
