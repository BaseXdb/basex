package org.basex.api.jaxrx;

import org.basex.BaseXServer;
import org.jaxrx.StartJettyServer;

/**
 * This class is responsible to start the database server and the
 * Jetty server, which is embedded in JAX-RX.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Lukas Lewandowski
 * @author Christian Gruen
 */
public final class StartJAXRX {
  /** Private constructor. */
  private StartJAXRX() { }
  
  /**
   * This main method starts the JAXRX implementation.
   * @param args (ignored) command-line parameters.
   */
  public static void main(final String[] args) {
    // start database server (if not done yet)
    new BaseXServer();

    // database server properties
    System.setProperty("org.jaxrx.port", "1984");
    System.setProperty("org.jaxrx.user", "admin");
    System.setProperty("org.jaxrx.password", "admin");

    // path to implementation package
    System.setProperty("org.jaxrx.implementation", "org.basex.api.jaxrx");
    // name of implementation
    System.setProperty("org.jaxrx.systemName", "basex");
    // port of web server
    System.setProperty("org.jaxrx.serverport", "8984");

    // start Jetty web server
    StartJettyServer.main(null);
  }
}
