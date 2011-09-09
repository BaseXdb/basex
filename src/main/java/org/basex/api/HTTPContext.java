package org.basex.api;

import static org.basex.api.HTTPText.*;

import org.basex.core.Context;

/**
 * This is a container for HTTP context information.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class HTTPContext {
  /** Single instance. */
  private static final HTTPContext INSTANCE = new HTTPContext();

  /** Database context. */
  public final Context context = new Context();

  /** Default user name. */
  String user;
  /** Default password. */
  String pass;
  /** Client flag. Default is {@code false} (standalone mode). */
  boolean client;

  /** Private constructor. */
  private HTTPContext() {
    update();
  }

  /**
   * Returns the singleton instance.
   * @return singleton instance
   */
  public static HTTPContext get() {
    return INSTANCE;
  }

  /**
   * Updates the system property assignments.
   */
  public void update() {
    user = System.getProperty(DBUSER);
    pass = System.getProperty(DBPASS);
    final String c = System.getProperty(DBCLIENT);
    client = c != null && c.equals("true");
  }

  /**
   * Returns a session.
   * @param u user
   * @param p password
   * @return session
   */
  public HTTPSession session(final String u, final String p) {
    final String us = user != null ? user : u;
    final String pa = pass != null ? pass : p;
    return new HTTPSession(this, us, pa);
  }
}
