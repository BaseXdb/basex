package org.basex.api.jaxrx;

import java.util.Properties;
import org.basex.BaseXServer;
import org.basex.core.Prop;
import org.basex.core.Text;
import org.jaxrx.JettyServer;

/**
 * This class initializes the JAX-RX/REST implementation.
 * A database server and the Jetty server is launched by the constructor.
 * The Jetty server listens for HTTP requests, which are then sent to JAX-RX.
 *
 * The following system properties can either be specified before the class
 * is launched, or passed on to the {@link #JaxRxServer(Properties)}
 * constructor:
 * <ul>
 * <li>{@code org.basex.user} (default: {@code admin})</li>
 * <li>{@code org.basex.password} (default: {@code admin})</li>
 * <li>{@code org.basex.serverport} (default: {@code 1984})</li>
 * <li>{@code org.basex.webport} (default: {@code 8984})</li>
 * <li>{@code org.basex.jaxrxpath} (default: <i>current directory</i>)</li>
 * <li>{@code org.jaxrx.parameter.output} (default: {@code indent=yes})</li>
 * </ul>
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
public final class JaxRxServer {
  /** Database server. */
  private BaseXServer server;
  /** Web server. */
  private JettyServer jetty;

  /**
   * Main method, launching the JAX-RX/REST implementation.
   * @param args (ignored) command-line parameters
   */
  public static void main(final String[] args) {
    new JaxRxServer();
  }

  /**
   * Constructor, using default properties.
   */
  public JaxRxServer() {
    this(null);
  }

  /**
   * Constructor with specified properties.
   * @param props properties
   */
  public JaxRxServer(final Properties props) {
    System.setProperties(props);

    // set default properties
    set("org.basex.user", Text.ADMIN);
    set("org.basex.password", Text.ADMIN);
    set("org.basex.serverport", Prop.SERVERPORT[1].toString());
    set("org.basex.webport", String.valueOf(8984));
    set("org.basex.jaxrxpath", "");
    set("org.jaxrx.parameter.output", "indent=yes");

    // define path and name of the JAX-RX implementation.
    set("org.jaxrx.systemName", Text.NAMELC);
    set("org.jaxrx.systemPath", BXJaxRx.class.getName());

    // start database server (if not done yet)
    server = new BaseXServer();

    // start Jetty server
    final int port = Integer.parseInt(System.getProperty("org.basex.webport"));
    jetty = new JettyServer(port);
  }

  /**
   * Stops the servers.
   */
  public void stop() {
    server.stop();
    jetty.stop();
  }

  /**
   * Sets the specified value if property has not been set yet.
   * @param key property key
   * @param value property value
   */
  void set(final String key, final String value) {
    if(System.getProperty(key) == null) System.setProperty(key, value);
  }
}
