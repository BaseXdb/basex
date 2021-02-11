package org.basex.http;

import java.util.concurrent.*;

import javax.servlet.http.*;

import org.basex.util.list.*;

/**
 * This class creates and destroys HTTP sessions.
 *
 * @author BaseX Team 2005-21, BSD License
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
    SESSIONS.remove(event.getSession().getId());
  }

  /**
   * Returns the ids of all connected sessions.
   * @return client ids
   */
  public static TokenList ids() {
    final TokenList ids = new TokenList(SESSIONS.size());
    for(final String key : SESSIONS.keySet()) ids.add(key);
    return ids;
  }

  /**
   * Returns the session with the specified id.
   * @param id session id
   * @return session
   */
  public static HttpSession get(final String id) {
    return SESSIONS.get(id);
  }
}
