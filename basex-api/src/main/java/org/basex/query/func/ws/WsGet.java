package org.basex.query.func.ws;

import org.basex.http.ws.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class WsGet extends WsFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final WebSocket client = client(qc);
    final String name = toString(arg(1), qc);

    final Value value = client.atts.get(name);
    if(value != null) return value;

    return defined(2) ? arg(2).value(qc) : Empty.VALUE;
  }
}
