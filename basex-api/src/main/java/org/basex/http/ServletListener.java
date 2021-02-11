package org.basex.http;

import javax.servlet.*;

/**
 * This class creates and destroys servlet contexts.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class ServletListener implements ServletContextListener {
  @Override
  public void contextInitialized(final ServletContextEvent event) {
  }

  @Override
  public void contextDestroyed(final ServletContextEvent event) {
    HTTPContext.get().close();
  }
}
