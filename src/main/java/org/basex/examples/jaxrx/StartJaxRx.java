package org.basex.examples.jaxrx;

import org.basex.BaseXServer;
import org.jaxrx.StartJetty;

/**
 * This class demonstrates how the REST implementation, based on JAX-RX, can be
 * used. A database server and the Jetty server is launched. The Jetty server
 * listens for HTTP requests, which are then sent to JAX-RX.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class StartJaxRx {
  /** Private constructor. */
  private StartJaxRx() { }

  /**
   * This main method starts the JAXRX implementation.
   * @param args (ignored) command-line parameters
   */
  public static void main(final String[] args) {
    // Start database server (if not done yet).
    new BaseXServer();

    // Set database user, password and server port;
    // avoid plain-text representation in your own code.
    System.setProperty("org.basex.user", "admin");
    System.setProperty("org.basex.password", "admin");
    System.setProperty("org.basex.serverport", "1984");

    // Server path to query files, which are launched by the 'run' parameter.
    System.setProperty("org.basex.jaxrxpath", "etc/");

    // Define name of, and path to JAX-RX implementation.
    System.setProperty("org.jaxrx.systemName", "basex");
    System.setProperty("org.jaxrx.systemPath", "org.basex.api.jaxrx.BXJaxRx");

    // Optional pre-declarations of query parameters.
    System.setProperty("org.jaxrx.parameter.output", "indent=yes");

    // Start Jetty server on the specified port.
    new StartJetty(8489);
  }
}
