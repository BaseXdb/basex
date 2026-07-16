package org.basex.query.func.ws;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import org.basex.http.ws.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.*;
import org.eclipse.jetty.websocket.api.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class WsClose extends WsFn {
  /** Maximum length of a close reason (RFC 6455): 123 bytes. */
  private static final int MAX_REASON = 123;

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final WebSocket client = client(qc);
    final long status = defined(1) ? toLong(arg(1), qc) : StatusCode.NORMAL;
    if(status < 1000 || status > 4999)
      throw BASEX_WS_X.get(info, "Invalid close status: " + status);
    final String reason = toStringOrNull(arg(2), qc);
    if(reason != null && token(reason).length > MAX_REASON)
      throw BASEX_WS_X.get(info, "Close reason exceeds " + MAX_REASON + " bytes.");
    client.close((int) status, reason);
    return Empty.VALUE;
  }
}
