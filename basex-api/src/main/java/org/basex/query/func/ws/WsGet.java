package org.basex.query.func.ws;

import org.basex.http.ws.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class WsGet extends WsFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final WebSocket client = client(qc);
    final String name = Token.string(toToken(exprs[1], qc));
    final Value dflt = exprs.length == 2 ? Empty.VALUE : exprs[2].value(qc);

    final Value value = client.atts.get(name);
    return value != null ? value : dflt;
  }
}
