/**
 * 
 */
package org.basex.api.jaxrx;

import org.jaxrx.StartJettyServer;

/**
 * This class is responsible to start the Jetty server which is in JAX-RX
 * embedded.
 * 
 * @author Lukas Lewandowski, University of Konstanz
 * 
 */
public class StartServer {

  /**
   * This main method starts the embedded Jetty server within JAX-RX.
   * 
   * @param args Not used parameters.
   */
  public static void main(String[] args) {
    System.setProperty("org.jaxrx.implementation", "org.basex.api.jaxrx");
    System.setProperty("org.jaxrx.systemName", "basex");
    System.setProperty("org.jaxrx.serverport", "8092");
    StartJettyServer.main(null);

  }

}
