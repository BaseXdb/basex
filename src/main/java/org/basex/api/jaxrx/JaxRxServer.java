package org.basex.api.jaxrx;

import static org.basex.api.HTTPText.*;

import org.basex.api.HTTPContext;
import org.basex.core.Prop;
import org.basex.core.Text;
import org.jaxrx.JettyServer;
import org.jaxrx.core.JaxRxConstants;
import org.mortbay.jetty.Server;

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
  /**
   * Constructor.
   * @param server jetty server
   */
  public JaxRxServer(final Server server) {
    // set serializer options (handled within the JAX-RX interface)
    final HTTPContext http = HTTPContext.get();
    System.setProperty(SERIALIZER, http.context.prop.get(Prop.SERIALIZER));

    // define path and name of the JAX-RX implementation.
    System.setProperty(JaxRxConstants.NAMEPROP, Text.NAMELC);
    System.setProperty(JaxRxConstants.PATHPROP, BXJaxRx.class.getName());

    JettyServer.register(server);
  }
}
