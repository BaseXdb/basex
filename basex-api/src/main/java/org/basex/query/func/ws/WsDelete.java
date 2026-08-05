package org.basex.query.func.ws;

import org.basex.http.ws.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class WsDelete extends WsFn {
  @Override
  protected Item item(final QueryContext qc) throws QueryException {
    final WebSocket client = client(qc);
    final String key = toString(arg(1), qc);
    client.atts.remove(key);
    return Empty.VALUE;
  }
}
