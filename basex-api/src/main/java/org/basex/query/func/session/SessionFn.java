package org.basex.query.func.session;

import static org.basex.query.QueryError.*;

import jakarta.servlet.http.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.util.*;

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
    // check if HTTP connection is available
    final HttpServletRequest request = request(qc);

    // WebSocket context: access existing session
    HttpSession session = wsSession(qc);
    // HTTP context: get/create session
    if(session == null) {
      try {
        session = request.getSession(create);
      } catch(final NullPointerException ex) {
        // Jetty 12, getSession: _coreRequest may be null for propagated request instances
        Util.debug(ex);
      }
    }
    // no session created (may happen with WebSockets): raise error or return null reference
    if(session == null) {
      if(create) throw SESSION_NOTFOUND.get(info);
      return null;
    }
    return new ASession(session);
  }

  /**
   * Tries to return a WebSocket session instance.
   * @param qc query context
   * @return session instance or {@code null}
   */
  private static HttpSession wsSession(final QueryContext qc) {
    // accessed via reflection, as WebSockets are only supported for Jetty
    final Class<?> wsClass = Reflect.find("org.basex.http.ws.WebSocket");
    if(wsClass != null) {
      final Object ws = qc.context.getExternal(wsClass);
      if(ws != null) return (HttpSession) Reflect.get(Reflect.field(wsClass, "session"), ws);
    }
    return null;
  }
}
