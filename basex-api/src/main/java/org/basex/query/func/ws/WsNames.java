package org.basex.query.func.ws;

import org.basex.http.ws.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class WsNames extends WsFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final WebSocket client = client(qc);

    final TokenList names = new TokenList(client.atts.size());
    for(final String key : client.atts.keySet()) names.add(key);
    return StrSeq.get(names);
  }
}
