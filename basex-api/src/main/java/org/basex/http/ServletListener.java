package org.basex.http;

import jakarta.servlet.*;

/**
 * This class creates and destroys servlet contexts.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class ServletListener implements ServletContextListener {
  @Override
  public void contextDestroyed(final ServletContextEvent event) {
    HTTPContext.get().close();
  }
}
