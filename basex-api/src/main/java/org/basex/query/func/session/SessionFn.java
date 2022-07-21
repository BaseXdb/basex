package org.basex.query.func.session;

import static org.basex.query.QueryError.*;

import javax.servlet.http.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.util.*;

/**
 * Session function.
 *
 * @author BaseX Team 2005-22, BSD License
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
    if(session == null) session = request.getSession(create);
    // no session created (may happen with WebSockets): raise error or return null reference
    if(session == null) {
      if(create) throw SESSION_NOTFOUND.get(info);
      return null;
    }
    return new ASession(session);
  }

  /**
   * Tries to return a WebSocket session instance from the (if found in the classpath).
   * Accessed via reflection, as WebSockets are only supported for Jetty.
   * @param qc query context
   * @return session instance or {@code null}
   */
  private static HttpSession wsSession(final QueryContext qc) {
    final Class<?> wsClass = Reflect.find("org.basex.http.ws.WebSocket");
    if(wsClass != null) {
      final Object ws = qc.context.getExternal(wsClass);
      if(ws != null) return (HttpSession) Reflect.get(Reflect.field(wsClass, "session"), ws);
    }
    return null;
  }
}
