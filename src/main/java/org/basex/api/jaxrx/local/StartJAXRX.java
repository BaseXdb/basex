package org.basex.api.jaxrx.local;

import org.jaxrx.StartJettyServer;

/**
 * This class is responsible to start the Jetty server, which is
 * embedded in JAX-RX.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Lukas Lewandowski
 */
public final class StartJAXRX {
  /** Private constructor. */
  private StartJAXRX() { }
  
  /**
   * This main method starts the embedded Jetty server within JAX-RX.
   * @param args Not used parameters.
   */
  public static void main(final String[] args) {
    System.setProperty("org.jaxrx.implementation", "org.basex.api.jaxrx.local");
    System.setProperty("org.jaxrx.systemName", "basex");
    System.setProperty("org.jaxrx.serverport", "8984");
    StartJettyServer.main(null);
  }
}
