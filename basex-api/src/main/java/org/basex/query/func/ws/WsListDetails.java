package org.basex.query.func.ws;

import jakarta.servlet.http.*;

import org.basex.http.ws.*;
import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class WsListDetails extends WsFn {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final String id = toStringOrNull(arg(0), qc);

    final TokenList ids = id != null ? new TokenList(1).add(id) : WsPool.ids();
    final ValueBuilder vb = new ValueBuilder(qc);
    for(final byte[] key : ids) {
      final String client = Token.string(key);
      final WebSocket ws = WsPool.get(client);
      // skip connections that were closed while the list was being built
      if(ws == null) continue;

      final FBuilder elem = FElem.build(Q_WEBSOCKET);
      elem.attr(Q_ID, client).attr(Q_PATH, ws.path);
      elem.attr(Q_ADDRESS, ws.clientAddress()).attr(Q_USER, ws.clientName());
      final HttpSession session = ws.session;
      if(session != null) {
        try {
          elem.attr(Q_SESSION, session.getId());
        } catch(final IllegalStateException ex) {
          // session was invalidated in the meantime
          Util.debug(ex);
        }
      }
      if(ws.subprotocol != null) elem.attr(Q_SUBPROTOCOL, ws.subprotocol);
      elem.attr(Q_CREATED, Dtm.local(ws.created, info).string(info));
      elem.attr(Q_ACCESSED, Dtm.local(ws.accessed, info).string(info));
      vb.add(elem.finish());
    }
    return vb.value(this);
  }
}
