package org.basex.http.restxq;

import javax.servlet.http.*;

import org.basex.http.*;
import org.basex.query.*;
import org.basex.util.*;

/**
 * Information on RESTXQ singleton functions.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
final class RestXqSingleton {
  /** Mutex. */
  private static final Object MUTEX = new Object();

  /** Id of singleton function. */
  private final String id;
  /** Query context. */
  private final QueryContext qc;
  /** HTTP session. */
  private final HttpSession session;

  /**
   * Constructor for singleton functions.
   * @param conn HTTP connection
   * @param id id of singleton function
   * @param qc query context
   */
  RestXqSingleton(final HTTPConnection conn, final String id, final QueryContext qc) {
    this.qc = qc;
    this.id = id;
    session = conn.request.getSession();
    queue();
    register();
  }

  /**
   * Waits until a running query has been stopped.
   */
  private void queue() {
    final QueryContext oldQc = qc();
    if(oldQc != null) {
      oldQc.stop();
      do Performance.sleep(1); while(qc() == oldQc);
    }
  }

  /**
   * Registers a query.
   */
  private void register() {
    synchronized(MUTEX) {
      session.setAttribute(id, qc);
    }
  }

  /**
   * Unregisters a query.
   */
  void unregister() {
    synchronized(MUTEX) {
      if(qc == qc()) session.removeAttribute(id);
    }
  }

  /**
   * Returns a registered query context.
   * @return query context or {@code null}
   */
  private QueryContext qc() {
    synchronized(MUTEX) {
      try {
        final Object obj = session.getAttribute(id);
        return obj instanceof QueryContext ? (QueryContext) obj : null;
      } catch(final IllegalStateException ex) {
        // invalidated session (no other way to check this state)
        Util.debug(ex);
        return null;
      }
    }
  }
}
