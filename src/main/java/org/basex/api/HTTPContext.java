package org.basex.api;

import org.basex.core.Context;

/**
 * This is a container for HTTP context information.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class HTTPContext {
  /** Single instance. */
  public static final HTTPContext INSTANCE = new HTTPContext();
  /** Database context. */
  public final Context context = new Context();
  /** Client flag: start server or standalone mode. */
  public boolean client;

  /** Private constructor. */
  private HTTPContext() { }
}
