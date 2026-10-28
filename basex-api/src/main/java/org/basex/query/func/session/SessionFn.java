package org.basex.query.func.session;

import static org.basex.query.QueryError.*;

import jakarta.servlet.http.*;

import org.basex.http.*;
import org.basex.query.*;
import org.basex.query.func.*;

/**
 * Session function.
 *
 * @author BaseX Team, BSD License
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
    // WebSocket context: access existing session
    HttpSession session = qc.context.getExternal(WsSession.class) instanceof final WsSession ws ?
      ws.session() : null;
    // HTTP context: get/create session (not available for detached requests)
    if(session == null) session = state(qc).session(create);
    if(session != null) return new ASession(session, null, info);
    // no session created (may happen with WebSockets): raise error or return null reference
    if(create) throw SESSION_NOTFOUND.get(info);
    return null;
  }
}
