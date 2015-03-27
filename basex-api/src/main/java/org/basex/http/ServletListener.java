package org.basex.http;

import javax.servlet.*;
import javax.servlet.annotation.*;

/**
 * <p>Servlet listener.</p>
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
@WebListener
public abstract class ServletListener implements ServletContextListener {
  @Override
  public void contextInitialized(final ServletContextEvent event) {
  }

  @Override
  public void contextDestroyed(final ServletContextEvent event) {
    HTTPContext.close();
  }
}
