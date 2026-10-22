package org.basex.http;

import java.util.concurrent.*;

import jakarta.servlet.http.*;

import org.basex.http.ws.*;
import org.basex.util.list.*;

/**
 * This class creates and destroys HTTP sessions.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class SessionListener implements HttpSessionListener {
  /** Sessions. */
  private static final ConcurrentHashMap<String, HttpSession> SESSIONS = new ConcurrentHashMap<>();

  @Override
  public void sessionCreated(final HttpSessionEvent event) {
    final HttpSession sess =  event.getSession();
    SESSIONS.put(sess.getId(), sess);
  }

  @Override
  public void sessionDestroyed(final HttpSessionEvent event) {
    final HttpSession sess = event.getSession();
    SESSIONS.remove(sess.getId());
    WsPool.close(sess);
  }

  /**
   * Returns the IDs of all connected sessions.
   * @return session IDs
   */
  public static TokenList ids() {
    final TokenList ids = new TokenList(SESSIONS.size());
    for(final String key : SESSIONS.keySet()) ids.add(key);
    return ids;
  }

  /**
   * Returns the session with the specified ID.
   * @param id session ID
   * @return session
   */
  public static HttpSession get(final String id) {
    return SESSIONS.get(id);
  }
}
