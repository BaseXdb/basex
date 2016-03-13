package org.basex.http;

import java.util.*;

import javax.servlet.http.*;

/**
 * This class creates and destroys HTTP sessions.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
public final class SessionListener implements HttpSessionListener {
  /** Sessions. */
  private static HashMap<String, HttpSession> sessions;

  @Override
  public void sessionCreated(final HttpSessionEvent event) {
    final HttpSession sess =  event.getSession();
    sessions().put(sess.getId(), sess);
  }

  @Override
  public void sessionDestroyed(final HttpSessionEvent event) {
    sessions().remove(event.getSession().getId());
  }

  /**
   * Initializes the HTTP context.
   * @return context;
   */
  public static synchronized HashMap<String, HttpSession> sessions() {
    if(sessions == null) sessions = new HashMap<>();
    return sessions;
  }
}
