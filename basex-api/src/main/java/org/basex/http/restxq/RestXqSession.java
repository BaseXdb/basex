package org.basex.http.restxq;

import javax.servlet.http.*;

import org.basex.http.*;
import org.basex.query.*;

/**
 * Information on a RESTXQ session.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
final class RestXqSession {
  /** HTTP session. */
  private final HttpSession session;
  /** Function id. */
  private final String key;
  /** Query context. */
  private final QueryContext qc;

  /**
   * Returns a query context stored in the current session.
   * @param http HTTP session
   * @param key function key
   * @param qc query context
   */
  RestXqSession(final HTTPContext http, final String key, final QueryContext qc) {
    this.qc = qc;
    this.key = key;
    session = http.req.getSession();

    if(key != null) {
      final Object oldQc = session.getAttribute(key);
      if(oldQc instanceof QueryContext) {
        ((QueryContext) oldQc).stop();
        do Thread.yield(); while(session.getAttribute(key) == oldQc);
      }
      session.setAttribute(key, qc);
    }
  }

  /**
   * Closes a session. Drops a previously cached query context.
   */
  void close() {
    if(key != null) {
      final Object oldQc = session.getAttribute(key);
      if(oldQc == qc) session.removeAttribute(key);
    }
  }
}
