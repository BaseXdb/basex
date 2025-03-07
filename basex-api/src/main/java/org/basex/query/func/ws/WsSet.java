package org.basex.query.func.ws;

import org.basex.http.ws.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class WsSet extends WsFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final WebSocket client = client(qc);
    final String key = toString(arg(1), qc);
    final Value value = arg(2).value(qc);

    client.atts.put(key, value.materialize(n -> false, info, qc));
    return Empty.VALUE;
  }
}
