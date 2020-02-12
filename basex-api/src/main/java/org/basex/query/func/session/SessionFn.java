package org.basex.query.func.session;

import static org.basex.query.QueryError.*;

import javax.servlet.http.*;

import org.basex.http.*;
import org.basex.http.ws.*;
import org.basex.query.*;
import org.basex.query.func.*;

/**
 * Session function.
 *
 * @author BaseX Team 2005-20, BSD License
 * @author Christian Gruen
 */
abstract class SessionFn extends ApiFunc {
  /**
   * Returns a session instance.
   * @param qc query context
   * @return session instance
   * @throws QueryException query exception
   */
  final ASession session(final QueryContext qc) throws QueryException {
    // check if HTTP connection is available
    final HttpServletRequest requestCtx = request(qc);

    // WebSocket context: access existing session
    HttpSession session = null;
    final Object ws = qc.getProperty(HTTPText.WEBSOCKET);
    if(ws != null) session = ((WebSocket) ws).session;
    // HTTP context: get/create session
    if(session == null) session = requestCtx.getSession();
    // raise error if no session could be created (may happen in the WebSocket context)
    if(session == null) throw SESSIONS_NOTFOUND.get(info);

    return new ASession(session);
  }
}
