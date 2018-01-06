package org.basex.http.restxq;

import javax.servlet.http.*;

import org.basex.http.*;
import org.basex.query.*;
import org.basex.util.*;

/**
 * Information on a RESTXQ session.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
final class RestXqSession {
  /** HTTP session. */
  private final HttpSession session;
  /** Id of singleton function ({@code null} if function is no singleton). */
  private final String singleton;
  /** Query context. */
  private final QueryContext qc;

  /**
   * Returns a query context stored in the current session.
   * @param conn HTTP connection
   * @param singleton id of singleton function
   * @param qc query context
   */
  RestXqSession(final HTTPConnection conn, final String singleton, final QueryContext qc) {
    this.qc = qc;
    this.singleton = singleton;
    session = conn.req.getSession();

    // singleton function: stop evaluation of existing function, wait until it has been finished
    if(singleton != null) {
      final Object oldQc = session.getAttribute(singleton);
      if(oldQc instanceof QueryContext) {
        ((QueryContext) oldQc).stop();
        do {
          Performance.sleep(1);
        } while(session.getAttribute(singleton) == oldQc);
      }
      session.setAttribute(singleton, qc);
    }
  }

  /**
   * Closes a session. Drops a previously cached query context.
   */
  void close() {
    if(singleton != null) {
      final Object oldQc = session.getAttribute(singleton);
      if(oldQc == qc) session.removeAttribute(singleton);
    }
  }
}
