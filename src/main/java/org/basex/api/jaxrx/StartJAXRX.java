package org.basex.api.jaxrx;

import org.basex.BaseXServer;
import org.basex.core.Prop;
import org.basex.core.Text;
import org.jaxrx.StartJetty;

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
    final BaseXServer server = new BaseXServer();
    final Prop prop = server.context.prop;

    // database user
    System.setProperty("org.jaxrx.user", "admin");
    // database password
    System.setProperty("org.jaxrx.password", "admin");

    // database server port
    System.setProperty("org.jaxrx.serverport",
        Integer.toString(prop.num(Prop.SERVERPORT)));
    // path to web directory (needed by the 'run' parameter)
    System.setProperty("org.jaxrx.webpath", prop.get(Prop.WEBPATH));

    // name of implementation(s)
    System.setProperty("org.jaxrx.systemName", Text.NAMELC);
    // path to implementation package(s)
    System.setProperty("org.jaxrx.implementation", 
        StartJAXRX.class.getPackage().getName());

    // start Jetty web server
    new StartJetty(prop.num(Prop.WEBPORT));
  }
}
