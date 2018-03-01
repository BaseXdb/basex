package org.basex.http.restxq;

import javax.servlet.http.*;

import org.basex.http.*;
import org.basex.query.*;
import org.basex.util.*;

/**
 * Information on RESTXQ singleton functions.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
final class RestXqSingleton {
  /** Mutex. */
  private static final Object MUTEX = new Object();

  /** Id of singleton function. */
  private final String singleton;
  /** Query context. */
  private final QueryContext qc;
  /** HTTP session. */
  private final HttpSession session;

  /**
   * Constructor for singleton functions.
   * @param conn HTTP connection
   * @param singleton id of singleton function
   * @param qc query context
   */
  RestXqSingleton(final HTTPConnection conn, final String singleton, final QueryContext qc) {
    this.qc = qc;
    this.singleton = singleton;
    session = conn.req.getSession();
    queue();
    register();
  }

  /**
   * Waits until a running query has been stopped.
   */
  void queue() {
    final QueryContext oldQc = qc();
    if(oldQc != null) {
      oldQc.stop();
      do Performance.sleep(1); while(qc() == oldQc);
    }
  }

  /**
   * Registers a query.
   */
  void register() {
    synchronized(MUTEX) {
      session.setAttribute(singleton, qc);
    }
  }

  /**
   * Unregisters a query.
   */
  void unregister() {
    synchronized(MUTEX) {
      if(qc == qc()) session.removeAttribute(singleton);
    }
  }

  /**
   * Returns a registered query context.
   * @return query context or {@code null}
   */
  private QueryContext qc() {
    synchronized(MUTEX) {
      final Object obj = session.getAttribute(singleton);
      return obj instanceof QueryContext ? (QueryContext) obj : null;
    }
  }
}
