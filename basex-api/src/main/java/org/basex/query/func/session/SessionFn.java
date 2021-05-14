package org.basex.query.func.session;

import static org.basex.query.QueryError.*;

import javax.servlet.http.*;

import org.basex.http.ws.*;
import org.basex.query.*;
import org.basex.query.func.*;

/**
 * Session function.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
abstract class SessionFn extends ApiFunc {
  /**
   * Returns a session instance.
   * @param qc query context
   * @param create create session if none exists
   * @return session instance or {@code null}
   * @throws QueryException query exception
   */
  final ASession session(final QueryContext qc, final boolean create) throws QueryException {
    // check if HTTP connection is available
    final HttpServletRequest request = request(qc);

    // WebSocket context: access existing session
    HttpSession session = null;
    final WebSocket ws = (WebSocket) qc.context.getExternal(WebSocket.class);
    if(ws != null) session = ws.session;
    // HTTP context: get/create session
    if(session == null) session = request.getSession(create);
    // no session created (may happen with WebSockets): raise error or return null reference
    if(session == null) {
      if(create) throw SESSION_NOTFOUND.get(info);
      return null;
    }
    return new ASession(session);
  }
}
