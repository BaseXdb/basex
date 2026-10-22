package org.basex.http;

import jakarta.servlet.*;

/**
 * This class creates and destroys servlet contexts.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class ServletListener implements ServletContextListener {
  /** SameSite cookie attribute. */
  private static final String SAMESITE = "SameSite";

  @Override
  public void contextInitialized(final ServletContextEvent event) {
    // cross-site requests: do not send session cookies, unless configured otherwise in web.xml
    final SessionCookieConfig config = event.getServletContext().getSessionCookieConfig();
    if(config.getAttribute(SAMESITE) == null) config.setAttribute(SAMESITE, "Lax");
  }

  @Override
  public void contextDestroyed(final ServletContextEvent event) {
    HTTPContext.get().close();
  }
}
