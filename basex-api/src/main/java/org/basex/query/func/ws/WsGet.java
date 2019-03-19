package org.basex.query.func.ws;

import org.basex.http.ws.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class WsGet extends WsFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final WebSocket client = client(qc);
    final String key = Token.string(toToken(exprs[1], qc));

    final Value value = client.atts.get(key);
    return value != null ? value : exprs.length == 2 ? Empty.SEQ : exprs[2].value(qc);
  }
}
