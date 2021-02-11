package org.basex.query.func.sessions;

import static org.basex.query.QueryError.*;

import javax.servlet.http.*;

import org.basex.http.*;
import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.func.session.*;
import org.basex.util.*;

/**
 * Sessions function.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
abstract class SessionsFn extends ApiFunc {
  /**
   * Checks permissions.
   * @param qc query context
   * @throws QueryException query exception
   */
  final void check(final QueryContext qc) throws QueryException {
    checkAdmin(qc);
    // check if HTTP connection is available
    request(qc);
  }

  /**
   * Returns a session instance.
   * @param qc query context
   * @return session instance
   * @throws QueryException query exception
   */
  final ASession session(final QueryContext qc) throws QueryException {
    check(qc);

    // retrieve session from global listener
    final byte[] id = toToken(exprs[0], qc);
    final HttpSession session = SessionListener.get(Token.string(id));
    if(session == null) throw SESSIONS_NOTFOUND_X.get(info, id);
    return new ASession(session);
  }
}
