package org.basex.api.jaxrx;

import static org.basex.api.HTTPText.*;

import org.basex.api.HTTPContext;
import org.basex.core.Context;
import org.basex.core.MainProp;
import org.basex.core.Prop;
import org.basex.core.Text;
import org.jaxrx.JettyServer;
import org.jaxrx.core.JaxRxConstants;

/**
 * This is the starter class for running the JAX-RX server, based on the JAX-RX
 * interface. A database server and the Jetty server is launched by the
 * constructor. The Jetty server listens for HTTP requests, which are then sent
 * to JAX-RX.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class JaxRxServer {
  /** Jetty server. */
  private final JettyServer jetty;

  /**
   * Constructor.
   * @throws Exception exception
   */
  public JaxRxServer() throws Exception {
    // set serializer options (handled within the JAX-RX interface)
    final Context context = HTTPContext.get().context;
    set(SERIALIZER, context.prop.get(Prop.SERIALIZER), false);

    // define path and name of the JAX-RX implementation.
    set(JaxRxConstants.NAMEPROP, Text.NAMELC, false);
    set(JaxRxConstants.PATHPROP, BXJaxRx.class.getName(), false);

    // start Jetty server
    jetty = new JettyServer(context.mprop.num(MainProp.JAXRXPORT));
  }

  /**
   * Stops the server.
   */
  public void stop()  {
    jetty.stop();
  }

  /**
   * Sets the specified value if property has not been set yet.
   * @param key property key
   * @param value property value
   * @param force force setting
   */
  private void set(final String key, final Object value, final boolean force) {
    if(force || System.getProperty(key) == null) {
      System.setProperty(key, value.toString());
    }
  }
}
