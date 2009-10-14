package org.basex.server;

/**
 * This is a simple container for processes from clients.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Andreas Weiler
 */
public class ClientProcess {
  /** Server session. */
  ServerSession sess;
  /** Boolean if updating. */
  boolean updating;
  
  /**
   * Standard constructor.
   * @param s ServerSession
   * @param u boolean
   */
  public ClientProcess(final ServerSession s, final boolean u) {
    this.sess = s;
    this.updating = u;
  }
}
