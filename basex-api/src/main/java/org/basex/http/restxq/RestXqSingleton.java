package org.basex.http.restxq;

import jakarta.servlet.http.*;

import org.basex.http.*;
import org.basex.query.*;
import org.basex.util.*;

/**
 * Information on RESTXQ singleton functions.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
final class RestXqSingleton {
  /** Mutex. */
  private static final Object MUTEX = new Object();

  /** ID of singleton function. */
  private final String id;
  /** Query context. */
  private final QueryContext qc;
  /** HTTP session. */
  private final HttpSession session;

  /**
   * Constructor for singleton functions.
   * @param conn HTTP connection
   * @param id ID of singleton function
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
      do Performance.sleep(10); while(qc() == oldQc);
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
      return HTTPConnection.getAttribute(session, id)
          instanceof final QueryContext qctx ? qctx : null;
    }
  }
}
