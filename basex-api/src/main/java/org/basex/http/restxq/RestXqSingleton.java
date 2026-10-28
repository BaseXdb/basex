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
  /** Lock that guards the session registry and serves as condition for {@link #queue()}. */
  private static final Object MUTEX = new Object();
  /** Maximum wait time between liveness checks (ms); safeguard against lost notifications. */
  private static final long WAIT = 1000;

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
    session = conn.requestCtx.state().session(true);
    synchronized(MUTEX) {
      queue();
      RequestState.attribute(session, id, qc);
    }
  }

  /**
   * Stops a running query (if present) and waits until it has been unregistered.
   */
  private void queue() {
    final QueryContext oldQc = qc();
    if(oldQc == null) return;

    // request the running query to stop, wait until it deregisters itself
    oldQc.stop();
    while(qc() == oldQc) {
      try {
        MUTEX.wait(WAIT);
      } catch(final InterruptedException ex) {
        Util.debug(ex);
      }
    }
  }

  /**
   * Unregisters a query.
   */
  void unregister() {
    synchronized(MUTEX) {
      if(qc == qc()) {
        RequestState.remove(session, id);
        MUTEX.notifyAll();
      }
    }
  }

  /**
   * Returns a registered query context.
   * @return query context or {@code null}
   */
  private QueryContext qc() {
    return RequestState.attribute(session, id) instanceof final QueryContext qctx ? qctx : null;
  }
}
